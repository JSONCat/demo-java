package com.sas.core.util.wxpay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.ConfigConstant.ConfigId;
import com.sas.core.constant.OrderConstant.TransactionOperation;
import com.sas.core.constant.SasWechatNotificationConstant;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.constant.TransactionConstant.OrderPayType;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasMarketBargainActivity;
import com.sas.core.meta.SasMarketBargainPerson;
import com.sas.core.meta.SasMarketGroupCheapActivity;
import com.sas.core.meta.SasMarketGroupCheapPerson;
import com.sas.core.meta.SasMemberUpgradeSetting;
import com.sas.core.meta.SasMenuActivity;
import com.sas.core.meta.SasMenuActivityOrder;
import com.sas.core.meta.SasMenuGoodOrder;
import com.sas.core.meta.SasMenuGoodOrderItem;
import com.sas.core.meta.SasOrderTransaction;
import com.sas.core.meta.SasRefundOrder;
import com.sas.core.meta.SasWxAppSetting;
import com.sas.core.meta.thirdpart.WechatJSTicketDTO;
import com.sas.core.service.PhoneMessageService;
import com.sas.core.service.SasTransactionService;
import com.sas.core.util.HtmlUtil;
import com.sas.core.util.IOUtil;
import com.sas.core.util.JsonUtil;
import com.sas.core.util.MathUtil;
import com.sas.core.util.ThreadUtil;
import com.sas.core.util.TimeUtil;
import com.sas.core.util.TransactionUtil;
import com.sas.core.util.wxpay.meta.AppWXPrePayDTO;
import com.sas.core.util.wxpay.meta.CloseReqData;
import com.sas.core.util.wxpay.meta.RefundQueryReqData;
import com.sas.core.util.wxpay.meta.RefundReqData;
import com.sas.core.util.wxpay.meta.ReverseReqData;
import com.sas.core.util.wxpay.meta.ScanPayQueryReqData;
import com.sas.core.util.wxpay.meta.ScanPayReqData;
import com.sas.core.util.wxpay.service.CloseService;
import com.sas.core.util.wxpay.service.RefundQueryService;
import com.sas.core.util.wxpay.service.RefundService;
import com.sas.core.util.wxpay.service.ReverseService;
import com.sas.core.util.wxpay.service.ScanPayQueryService;
import com.sas.core.util.wxpay.service.ScanPayService;

/**************
 * 微信支付util
 * 
 * @author Administrator
 */
public class WXPayUtil {

	final private static Logger logger = Logger.getLogger(WXPayUtil.class);
	
	public static final Logger orderLogger = Logger.getLogger("com.logger.order");
	
	/********************
	 * 判断微信回调签名的正确性
	 * @param params
	 * @return
	 */
	public static final boolean isCallbackSignatureRight(final Map<String, String> params, final String key)
	{
		final Map<String, String> signParams = new HashMap<String, String>();
		String returnSign = "";
		for(final Entry<String, String> entry : params.entrySet())
		{
			if("sign".equalsIgnoreCase(entry.getKey())){
				returnSign = entry.getValue();
			} else if(entry.getValue() != null){
				signParams.put(entry.getKey(), entry.getValue());
			}
		}
		return returnSign.length() > 0 && WXSignature.getSign(signParams, key).equalsIgnoreCase(returnSign);
	}
	
	/*****************
	 * 生成wap微信支付需要用到的token
	 * @param accessToken
	 * @param noncestr
	 * @param url
	 * @return
	 */
	public static final BinaryEntry<String, Long> generateJsapiTicketByAccessToken(final String accessToken, final int maxTryTimes) {
		int tryTimes = 0;
		do{
			String returnData = null;
			try {
				HttpClient httpClient = new HttpClient();
				httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Encoding.UTF8.type);
				GetMethod postMethod = new GetMethod("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + accessToken + "&type=jsapi");
				if(httpClient.executeMethod(postMethod) != 200){
					postMethod.abort();
					logger.error("Failed to executeMethod while getJsapiTicketByAccessToken!");
					return null;
				}
				returnData = postMethod.getResponseBodyAsString();
				final Map<String, Object> resultMap = JsonUtil.getObject(returnData, Map.class);
				if(resultMap.containsKey("errcode") && (Integer)resultMap.get("errcode") != 0){
					logger.error("Failed to getJsapiTicketByAccessToken because of code : " + resultMap.get("errcode")
							+ ", and error : " + resultMap.get("errmsg") + ", returnData=" + returnData);
					postMethod.abort();
					return null;
				}
				postMethod.abort();
				final Integer expireInSeconds = (Integer) resultMap.get("expires_in");
				return new BinaryEntry<String, Long>((String) resultMap.get("ticket"), 
						(expireInSeconds == null ? System.currentTimeMillis()*Miliseconds.TenMinutes.miliseconds
								: System.currentTimeMillis() + expireInSeconds.intValue()*Miliseconds.OneSecond.miliseconds));
			} catch (Exception e) {
				logger.error("Failed to getJsapiTicketByAccessToken with IOException : returnData=" + returnData, e);
				ThreadUtil.sleepNoException(Miliseconds.OneSecond.miliseconds * (++tryTimes));
			}
		}while(tryTimes < maxTryTimes);
		logger.error("Failed to getJsapiTicketByAccessToken with IOException, access max times " + tryTimes);
		return null;
	}
	
	/*****************
	 * 生成wap微信支付需要用到的相关key信息
	 * @param accessToken
	 * @param noncestr
	 * @param url
	 * @return
	 */
	public static final WechatJSTicketDTO createResponseDataWithJSTicket(final String jsTicket, final String noncestr, final String url) {
		final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
		final MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder("SHA-1");
		final String sha1Hex = encoder.encodePassword(
			"jsapi_ticket=" + jsTicket +
			"&noncestr=" + noncestr +
			"&timestamp=" + timestamp +
			"&url=" + url
		, "");
		return new WechatJSTicketDTO(jsTicket, noncestr,
				timestamp, url, sha1Hex, 0);
	}

	/*****************
	 * 生成微信接口的body
	 * @param name
	 * @param orderCode
	 * @return
	 */
	public static final String generateWXPayGoodName(final String name)
	{
		if(name.length() >= 32){
			return HtmlUtil.subStringWhenAccessLimit(name.replaceAll("=", ""), 32);
		}else {
			return name; 
		}		
	}
	
	/************
	 * 请求生成微信支付的pre pay id
	 */
	public static final AppWXPrePayDTO generateWXMiniProgramPayPrepayId(final SasWxAppSetting setting, final SasMenuActivity activity, final SasMenuActivityOrder order,
			final SasOrderTransaction transaction, final String openId) 
	{	
		if(transaction == null){
			return null;
		}
		final ScanPayReqData scanPayReqData = new ScanPayReqData(setting.getAppId(), setting.getAppPayMchId(),//生成请求支付链接参数
				String.valueOf(order.getId()),
				String.valueOf(transaction.getId()),
				WXPayUtil.generateWXPayGoodName(TransactionOperation.ActivityApply.name + activity.getTitle()),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				IOUtil.getLocalIP(),
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", WXPayConstant.TradeType.JSAPI.type, //"APP",
				openId, setting.getAppSignKey());
		String resultStr = null;
		try {
			resultStr = WXPayUtil.requestWXAppScanPayService(setting, scanPayReqData, 
					setting.getAppPayMchId());//调用请求支付接口，获取微信返回XML的信息
		} catch (Exception e) {
			logger.error("Failed to get resultStr while requestScanPayService with Exception : resultStr=" + resultStr, e);
		}
		String prepay_id = null;
		if(StringUtils.isNotBlank(resultStr)){
			try {
				prepay_id = (String)(XMLParser.getMapFromXML(resultStr).get("prepay_id"));
			} catch (Exception e) {
				logger.error("Failed to payActivityByWeixin with Exception : httpResult=" + resultStr, e);
			}
		}
		if(StringUtils.isBlank(prepay_id)){
			return null;
		}
		//生成签名
		final Map<String, String> signMap = new HashMap<String, String>();
		signMap.put("appId", setting.getAppId());
		signMap.put("signType", "MD5");
		signMap.put("nonceStr", RandomStringGenerator.getRandomStringByLength(32));
		signMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
		signMap.put("package", "prepay_id=" + prepay_id);
		signMap.put("sign", WXSignature.getSign(signMap, setting.getAppSignKey()));  
		return new AppWXPrePayDTO(prepay_id, signMap);
	}
	
	public static final String requestWxH5Pay(final Sas sas, final String attach, final SasOrderTransaction transaction,
																						final String spBillCreateIP){
	 final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				String.valueOf(transaction.getId()),
				transaction.getOrderGoodName(),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
        attach,
				spBillCreateIP,
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", WXPayConstant.TradeType.MWEB.type,
       "{'h5_info':{'type':'Wap','wap_url':'http://www.saihuitong.com','wap_name':'赛会通'}}");
		String resultStr = null;
		try {
			resultStr = WXPayUtil.requestScanPayService(scanPayReqData);
		} catch (Exception e) {
			logger.error("Failed to get resultStr while requestScanPayService with Exception : resultStr=" + resultStr, e);
			return null;
		}
		return resultStr;
  }
	
	/*******************
	 * 生成可以支付的二维码链接
	 * 
	 * @param sas
	 * @param good
	 * @param order
	 * @param transaction
	 * @return
	 */
	public static final String generateWXPayPrepayId(final Sas sas, final String attach,
			final SasOrderTransaction transaction, final String openId) 
	{
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				attach,
				String.valueOf(transaction.getId()),
				transaction.getOrderGoodName(),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				IOUtil.getLocalIP(),
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "JSAPI",
				openId);
		String resultStr = null;
		try {
			resultStr = WXPayUtil.requestScanPayService(scanPayReqData);
		} catch (Exception e) {
			logger.error("Failed to get resultStr while requestScanPayService with Exception : resultStr=" + resultStr, e);
			return null;
		}
		return resultStr;
	}
	
	/*******************
	 * 生成可以支付的二维码链接
	 * 
	 * @param sas
	 * @param good
	 * @param order
	 * @param transaction
	 * @return
	 */
	public static final String generateWXPayPrepayId(final Sas sas, final SasMenuActivity activity, final SasMenuActivityOrder order,
			final SasOrderTransaction transaction, final String openId) {
		return WXPayUtil.generateWXPayPrepayId(sas, String.valueOf(order.getId()),  transaction, openId);
	}
	

	public static final String generateWXPayPrepayId(final Sas sas, final SasMarketBargainActivity activity,
			final SasMarketBargainPerson person, final SasOrderTransaction transaction, final String openId) {
		return WXPayUtil.generateWXPayPrepayId(sas, String.valueOf(person.getId()),  transaction, openId);
	}

	public static final String generateWXPayPrepayId(final Sas sas, final SasMarketGroupCheapActivity activity,
			final SasMarketGroupCheapPerson person, final SasOrderTransaction transaction, final String openId) {
		return WXPayUtil.generateWXPayPrepayId(sas, String.valueOf(person.getId()),  transaction, openId);
	}

	/*******************
	 * 生成商品prepayId
	 * 
	 * @param sas
	 * @param good
	 * @param order
	 * @param transaction
	 * @return
	 */
	public static final String generateWXPayPrepayId(final Sas sas, final SasMenuGoodOrder order,
			final List<SasMenuGoodOrderItem> items, final SasOrderTransaction transaction, final String openId) {
		return WXPayUtil.generateWXPayPrepayId(sas, String.valueOf(order.getId()),  
				transaction, openId);
	}

	/************
	 * 请求生成微信二维码
	 * @param sas
	 * @param activity
	 * @param order
	 * @param transaction
	 * @param openId
	 * @return
	 */
	public static final String generateWXPayQRCode(final Sas sas, final SasMenuActivity activity, final SasMenuActivityOrder order,
			final SasOrderTransaction transaction, final String openId) {
		return WXPayUtil.generateWXPayQRCode(sas, String.valueOf(order.getId()), transaction, openId);
	}
	
	public static final String generateWXPayQRCode(final Sas sas, final SasMarketBargainPerson person,
			final SasOrderTransaction transaction, final String openId) {
		return WXPayUtil.generateWXPayQRCode(sas, String.valueOf(person.getId()), transaction, openId);
	}

	public static final String generateWXPayQRCode(final Sas sas, final SasMarketGroupCheapPerson person,
			final SasOrderTransaction transaction, final String openId) {
		return WXPayUtil.generateWXPayQRCode(sas, String.valueOf(person.getId()), transaction, openId);
	}
	
	public static final String generateWXPayQRCode(final Sas sas, final String attach,
			final SasOrderTransaction transaction, final String openId) 
	{	
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,//生成请求支付链接参数
				attach,
				String.valueOf(transaction.getId()),
				transaction.getOrderGoodName(),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				IOUtil.getLocalIP(),
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "NATIVE",
				openId);
		String resultStr = null;
		try {
			resultStr = WXPayUtil.requestScanPayService(scanPayReqData);//调用请求支付接口，获取微信返回XML的信息
		} catch (Exception e) {
			logger.error("Failed to get resultStr while requestScanPayService with Exception : resultStr=" + resultStr, e);
		}
		return resultStr;
	}
	
	/*************
	 * 生成微信 支付二维码
	 * @param sas
	 * @param setting
	 * @param transaction
	 * @param openId
	 * @return
	 */
	public static final String generateWXPayQRCode(final Sas sas, final SasMemberUpgradeSetting setting,
			final SasOrderTransaction transaction, final String openId)
	{
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,//生成请求支付链接参数
				String.valueOf(setting.getId()),
				String.valueOf(transaction.getId()),
				TransactionUtil.generateMemeberUpgradeGoodName(sas.getName(), setting),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				IOUtil.getLocalIP(),
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "NATIVE",
				openId);
		String resultStr = null;
		try {
			resultStr = WXPayUtil.requestScanPayService(scanPayReqData);//调用请求支付接口，获取微信返回XML的信息
		} catch (Exception e) {
			logger.error("Failed to get resultStr while requestScanPayService with Exception : resultStr=" + resultStr, e);
		}
		return resultStr;
	}
	
	
	/*******************
	 * 触发微信交易自动退款
	 * @param sas
	 * @param order
	 * @param transaction
	 * @return
	 */
	public static final void doWeixinPayRefundByAsynchronized(final long orderId, final String orderCode,
			final SasRefundOrder refundOrder,
			final SasOrderTransaction weixinPayTransaction, 
			final SasOrderTransaction refundTransaction, final int maxTryTimes,
			final PhoneMessageService phoneMessageService, final SasTransactionService sasTransactionService)
	{
		ThreadUtil.execute(new Runnable(){
			public void run()
			{
				final String wxOrderId = weixinPayTransaction.getThirdpartTransactionId();
				int times = 0;
				boolean isSuc = false;
				while(!isSuc && ++times<maxTryTimes){
					isSuc = WXPayUtil.doWeixinPayFund(orderId, 
							TransactionUtil.calculateTotalTransactionPrice(weixinPayTransaction), weixinPayTransaction.getId(),
							wxOrderId, refundTransaction);	
					if(isSuc){
						sasTransactionService.updateRefundOrderPaidState(refundOrder);
					}else{
						TransactionUtil.logRefundOrderMessage("WEIXIN-REFUND-RETRY", weixinPayTransaction.getOrderId(),
								weixinPayTransaction.getId(), "try times=" + times);
						ThreadUtil.sleepNoException( times * Miliseconds.FiveMinutes.miliseconds);
					}
				}
				if(isSuc){
					TransactionUtil.logRefundOrderMessage("WEIXIN-REFUND-SUCC", weixinPayTransaction.getOrderId(),
							weixinPayTransaction.getId(), "weixin refund succ finish");
				}else{
					phoneMessageService.sendMsg2NotifyErrorOrder(TransactionOperation.WeixinPayRefund, weixinPayTransaction.getSasId(),
							weixinPayTransaction.getOrderId(), 0, OrderPayType.WeixinPay.type,
							"微信退款-订单ID#" + weixinPayTransaction.getOrderId() 
							+ "-微信退款交易#" + refundTransaction.getId() +"-微信付款" + weixinPayTransaction.getId()
							+ "-金额￥" + TransactionUtil.calculateTotalTransactionPrice(weixinPayTransaction).toString());
					TransactionUtil.logRefundOrderMessage("WEIXIN-REFUND-FAIL", weixinPayTransaction.getOrderId(),
							weixinPayTransaction.getId(), "weixin refund fail finish");
				}
			}
		});
	}
	
	private static final boolean doWeixinPayFund(final long orderId, final BigDecimal totalTransactionPrice,
			final long lastOrderPaidTransactionId, final String wxOrderId, 
			final SasOrderTransaction refundTransaction) 
	{		
		final int totalFee = totalTransactionPrice.multiply(new BigDecimal(100)).intValue(); //如果是购物车， 就是总价 ,单位是分
		final int changePrice = refundTransaction.getChangePrice().multiply(new BigDecimal(100)).intValue(); //退款金额 单位是分
		final RefundReqData refundReqData = new RefundReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,wxOrderId, 
				String.valueOf(lastOrderPaidTransactionId),
				"deviceInof",
				String.valueOf(refundTransaction.getId()),
				totalFee,
				MathUtil.minInt(changePrice, totalFee), // 微信的单位是分
				"CNY",
				WXPayConstant.MchID);
		try {
			final String resultStr = WXPayUtil.requestRefundService(refundReqData);
			final Map<String, String> result = XMLParser.getMapFromXML(resultStr);
			if ("SUCCESS".equalsIgnoreCase(result.get("result_code"))){
				return true;
			}
			logger.error("Fail to doWeixinPayFund, resultStr=" +resultStr + ", order=" + orderId);
			return false;
		} catch (Exception e) {
			logger.error("Fail to get resultStr while requestRefundService with Exception, ex=" + e.getMessage(), e);
			return false;
		}
	}
	
	/*************
	 * 主动撤销没有支付的微信交易
	 * @param sas
	 * @param transaction
	 * @param isBieZhailaActivity
	 * @return
	 */
	public static final boolean doReverseTransaction(final SasOrderTransaction transaction, int tryTimes)
	{
		do{
			final ReverseReqData refundReqData = new ReverseReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,"", String.valueOf(transaction.getId()));
			try {
				final String resultStr = WXPayUtil.requestReverseService(refundReqData);
				final Map<String, String> result = XMLParser.getMapFromXML(resultStr);
				if("SUCCESS".equalsIgnoreCase(result.get("result_code"))){
					return true;
				}
				logger.error("Fail to doReverseTransaction, resultStr=" +resultStr + ", transaction=" + transaction.getId());
				return false;
			} catch (Exception e) {
				logger.error("Failed to get resultStr while doReverseTransaction with Exception, ex==" + e.getMessage(), e);
				ThreadUtil.sleepNoException(Miliseconds.TenSeconds.miliseconds);
			}
		}while(--tryTimes > 0);
		return false;		
	}
	
	/*************
	 * 主动关闭没有支付的微信交易
	 * @param sas
	 * @param transaction
	 * @param isBieZhailaActivity
	 * @return
	 */
	public static final boolean doCloseTransaction(final SasOrderTransaction transaction, int tryTimes)
	{
		do{
			final CloseReqData closeReqData = new CloseReqData(String.valueOf(transaction.getId()));
			try {
				final String resultStr = WXPayUtil.requestCloseService(closeReqData);
				final Map<String, String> result = XMLParser.getMapFromXML(resultStr);
				if("SUCCESS".equalsIgnoreCase(result.get("result_code"))){
					return true;
				}
				logger.error("Fail to doCloseTransaction, resultStr=" +resultStr + ", transaction=" + transaction.getId());
				return false;
			} catch (Exception e) {
				logger.error("Failed to get resultStr while doCloseTransaction with Exception, ex==" + e.getMessage(), e);
				ThreadUtil.sleepNoException(Miliseconds.TenSeconds.miliseconds);
			}
		}while(--tryTimes > 0);
		return false;		
	}
	
	/**************
	 * 生成微信支付二维码
	 * @param sas
	 * @param order
	 * @param items
	 * @param transaction
	 * @param openId
	 * @return
	 */
	public static final String generateWXPayQRCode(final Sas sas, final SasMenuGoodOrder order, final List<SasMenuGoodOrderItem> items,
			final SasOrderTransaction transaction, final String openId) {
		return WXPayUtil.generateWXPayQRCode(sas, String.valueOf(order.getId()), transaction, openId);
	}

	/*******************
	 * 生成商品prepayId
	 *
	 * @param sas
	 * @param setting
	 * @param transaction
	 * @return
	 */
	public static final String generateMemberUpgradeWXPayPrepayId(final Sas sas, final SasMemberUpgradeSetting setting,
													 final SasOrderTransaction transaction, final String openId) 
	{
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				String.valueOf(setting.getId()),
				String.valueOf(transaction.getId()),
				"购买"+setting.getMonthCount() + "个月会员",
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				IOUtil.getLocalIP(),
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "JSAPI",
				openId);
		String resultStr = null;
		try {
			resultStr = WXPayUtil.requestScanPayService(scanPayReqData);
		} catch (Exception e) {
			logger.error("Failed to get resultStr while requestScanPayService with Exception : resultStr=" + resultStr, e);
			return null;
		}
		return resultStr;
	}

	/**
	 * 请求支付服务
	 * 
	 * @param scanPayReqData
	 *            这个数据对象里面包含了API要求提交的各种数据字段
	 * @return API返回的数据
	 * @throws Exception
	 */
	public static String requestScanPayService(ScanPayReqData scanPayReqData) throws Exception {
		return new ScanPayService(WXPayConstant.CertPassword, WXPayUtil.class.getResource("/wxpay/apiclient_cert.p12").getPath()).request(scanPayReqData);
	}

	//微信小程序的支付
	public static String requestWXAppScanPayService(final SasWxAppSetting setting,
			ScanPayReqData scanPayReqData, final String certPassword) throws Exception {
	  if (useSasPay(setting)){
	    return new ScanPayService(certPassword, WXPayUtil.class.getResource("/wxpay/apiclient_cert.p12").getPath()).request(scanPayReqData);
    }else {
	    return new ScanPayService(certPassword, WXPayUtil.class.getResource("/wxpay/wxapp/"+
				setting.getAppId() +"/apiclient_cert.p12").getPath()).request(scanPayReqData);
    }
	}
	
	/**
	 * 请求支付查询服务
	 * 
	 * @param scanPayQueryReqData
	 *            这个数据对象里面包含了API要求提交的各种数据字段
	 * @return API返回的XML数据
	 * @throws Exception
	 */
	public static String requestScanPayQueryService(ScanPayQueryReqData scanPayQueryReqData) throws Exception {
		return new ScanPayQueryService(WXPayConstant.CertPassword, WXPayUtil.class.getResource("/wxpay/apiclient_cert.p12").getPath()).request(scanPayQueryReqData);
	}
	
    /**
     * 请求退款服务
     * @param refundReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
    public static String requestRefundService(RefundReqData refundReqData) throws Exception{
        return new RefundService(WXPayConstant.CertPassword, WXPayUtil.class.getResource("/wxpay/apiclient_cert.p12").getPath()).request(refundReqData);
    }

    /**
     * 请求退款查询服务
     * @param refundQueryReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
	public static String requestRefundQueryService(RefundQueryReqData refundQueryReqData) throws Exception{
		return new RefundQueryService(WXPayConstant.CertPassword, WXPayUtil.class.getResource("/wxpay/apiclient_cert.p12").getPath()).request(refundQueryReqData);
	}

	/**
     * 请求撤销服务
     * @param reverseReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
	public static String requestReverseService(ReverseReqData reverseReqData) throws Exception{
		return new ReverseService(WXPayConstant.CertPassword, WXPayUtil.class.getResource("/wxpay/apiclient_cert.p12").getPath()).request(reverseReqData);
	}

	/**
     * 请求关闭订单服务
     * @param reverseReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
	public static String requestCloseService(CloseReqData reqData) throws Exception{
		return new CloseService(WXPayConstant.CertPassword, WXPayUtil.class.getResource("/wxpay/apiclient_cert.p12").getPath()).request(reqData);
	}

	
	
	/**********
	 * 创建URL
	 * @param code
	 * @return
	 */
	public static final String generateOpenIdAccessTokenUrl(final String code){
		return "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+WXPayConstant.getAppID()+"&secret=" 
				+ WXPayConstant.getAppSecret() + "&code="
				+ code + "&grant_type=authorization_code";
	}
	
	/*********
	 * 生成微信通知发送的URL
	 * @param token
	 * @return
	 */
	public static final String generateWechatNotificationMessageSendURL(final String token)
	{
		return "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="
				+ token;
	}
	/*******
	 * 创建URL
	 * @return
	 */
	public static final String generateAccessTokenUrl(final ConfigId appId){
		if(appId == ConfigId.WeixinNotificationAppId){
			return "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
					+ SasWechatNotificationConstant.getAppIDWeixinNotification()
					+ "&secret="
					+ SasWechatNotificationConstant.getAppSecretWeixinNotification();
		}else {
			return "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
					+ WXPayConstant.getAppID()
					+ "&secret="
					+ WXPayConstant.getAppSecret();
		}
	}
  
  /**
   * 小程序是否使用赛会通微信支付
   *
   * @param setting
   * @return
   */
	public static final boolean useSasPay(final SasWxAppSetting setting){
		return setting != null && StringUtils.isNotBlank(setting.getAppPayMchId())
				&& WXPayConstant.MchID.equals(setting.getAppPayMchId());
	}
	
	public static final void main(String[] args)
	{
		final RefundReqData refundReqData = new RefundReqData(WXPayConstant.getAppID(), WXPayConstant.MchID, null, 
				"1025929",
				"deviceInof",
				"1002",
				100,
				100, // 微信的单位是分
				"CNY",
				"123");
		String resultStr = null;
		try {
			resultStr = WXPayUtil.requestRefundService(refundReqData);
			final Map<String, String> result = XMLParser.getMapFromXML(resultStr);
			System.out.println(result.get("return_code"));
			if(!"SUCCESS".equalsIgnoreCase(String.valueOf(result.get("return_code")))){
				System.out.println("000000-------------------");
			}
		} catch (Exception e) {
			logger.error("Failed to get resultStr while requestRefundService with Exception : resultStr=" + resultStr, e);

		}
	}

}