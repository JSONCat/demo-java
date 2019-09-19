package com.sas.core.util.wxpay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.OrderConstant.TransactionOperation;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.constant.TransactionConstant.OrderPayType;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasMarketBargainPerson;
import com.sas.core.meta.SasMarketGroupCheapPerson;
import com.sas.core.meta.SasMemberUpgradeSetting;
import com.sas.core.meta.SasMenuActivity;
import com.sas.core.meta.SasMenuActivityOrder;
import com.sas.core.meta.SasMenuGoodOrder;
import com.sas.core.meta.SasMenuGoodOrderItem;
import com.sas.core.meta.SasOrderTransaction;
import com.sas.core.meta.SasRefundOrder;
import com.sas.core.service.PhoneMessageService;
import com.sas.core.service.SasTransactionService;
import com.sas.core.util.IOUtil;
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
 * 别宅啦APP的微信支付util
 * 
 * @author Administrator
 */
public class AppWXPayUtil {

	final private static Logger logger = Logger.getLogger(AppWXPayUtil.class);
	
	public static final Logger orderLogger = Logger.getLogger("com.logger.order");
	
	/********************
	 * 判断微信回调签名的正确性
	 * @param params
	 * @return
	 */
	public static final boolean isCallbackSignatureRight(final Map<String, String> params)
	{
		return WXPayUtil.isCallbackSignatureRight(params, WXPayConstant.getKey());
	}

	/************
	 * 请求生成微信支付的pre pay id
	 * @param sas
	 * @param activity
	 * @param order
	 * @param transaction
	 * @param openId
	 * @return
	 */
	public static final AppWXPrePayDTO generateWXPayPrepayId(final Sas sas, final SasMenuActivity activity, final SasMenuActivityOrder order,
			final SasOrderTransaction transaction, final String openId) {
		return AppWXPayUtil.generateWXPayPrepayId(sas, String.valueOf(order.getId()), transaction, openId);
	}
	
	public static final AppWXPrePayDTO generateWXPayPrepayId(final Sas sas, final SasMarketBargainPerson person,
			final SasOrderTransaction transaction, final String openId) {
		return AppWXPayUtil.generateWXPayPrepayId(sas, String.valueOf(person.getId()), transaction, openId);
	}

	public static final AppWXPrePayDTO generateWXPayPrepayId(final Sas sas, final SasMarketGroupCheapPerson person,
			final SasOrderTransaction transaction, final String openId) {
		return AppWXPayUtil.generateWXPayPrepayId(sas, String.valueOf(person.getId()), transaction, openId);
	}
	
	public static final AppWXPrePayDTO generateWXPayPrepayId(final Sas sas, final String attach,
			final SasOrderTransaction transaction, final String openId) 
	{	
		final ScanPayReqData scanPayReqData = new ScanPayReqData(AppWXPayConstant.getAppID(), AppWXPayConstant.MchID,//生成请求支付链接参数
				attach,
				String.valueOf(transaction.getId()),
				transaction.getOrderGoodName(),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				IOUtil.getLocalIP(),
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/app/wxCallback/result", "APP",
				openId);
		String resultStr = null;
		try {
			resultStr = AppWXPayUtil.requestScanPayService(scanPayReqData);//调用请求支付接口，获取微信返回XML的信息
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
		signMap.put("appid", AppWXPayConstant.getAppID());
		signMap.put("partnerid", AppWXPayConstant.MchID);
		signMap.put("prepayid", prepay_id);
		signMap.put("noncestr", RandomStringGenerator.getRandomStringByLength(32));
		signMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
		signMap.put("package", "Sign=WXPay");
		final String sign = WXSignature.getSign(signMap);
		signMap.put("sign", sign);  
		return new AppWXPrePayDTO(prepay_id, signMap);
	}
	
	/*************
	 * 生成微信 支付二维码
	 * @param sas
	 * @param setting
	 * @param transaction
	 * @param openId
	 * @return
	 */
	public static final AppWXPrePayDTO generateWXPayPrepayId(final Sas sas, final SasMemberUpgradeSetting setting,
			final SasOrderTransaction transaction, final String openId) 
	{
		final ScanPayReqData scanPayReqData = new ScanPayReqData(AppWXPayConstant.getAppID(), AppWXPayConstant.MchID,//生成请求支付链接参数
				String.valueOf(setting.getId()),
				String.valueOf(transaction.getId()),
				TransactionUtil.generateMemeberUpgradeGoodName(sas.getName(), setting),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				IOUtil.getLocalIP(),
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/app/wxCallback/result", "APP",
				openId);
		String resultStr = null;
		try {
			resultStr = AppWXPayUtil.requestScanPayService(scanPayReqData);//调用请求支付接口，获取微信返回XML的信息
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
		signMap.put("appid", AppWXPayConstant.getAppID());
		signMap.put("partnerid", AppWXPayConstant.MchID);
		signMap.put("prepayid", prepay_id);
		signMap.put("noncestr", RandomStringGenerator.getRandomStringByLength(32));
		signMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
		signMap.put("package", "Sign=WXPay");
		final String sign = WXSignature.getSign(signMap);
		signMap.put("sign", sign);  
		return new AppWXPrePayDTO(prepay_id, signMap);
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
					isSuc = AppWXPayUtil.doWeixinPayFund(orderId, 
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
		final double totalFee = totalTransactionPrice.doubleValue(); //如果是购物车， 就是总价
		final RefundReqData refundReqData = new RefundReqData(AppWXPayConstant.getAppID(), AppWXPayConstant.MchID, wxOrderId, 
				String.valueOf(lastOrderPaidTransactionId),
				"deviceInof",
				String.valueOf(refundTransaction.getId()),
				(int)(totalFee * 100),
				(int)(MathUtil.minDouble(refundTransaction.getChangePrice().doubleValue(), totalFee)*100), // 微信的单位是分
				"CNY",
				AppWXPayConstant.MchID);
		try {
			final String resultStr = AppWXPayUtil.requestRefundService(refundReqData);
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
			final ReverseReqData refundReqData = new ReverseReqData(AppWXPayConstant.getAppID(), AppWXPayConstant.MchID,"", String.valueOf(transaction.getId()));
			try {
				final String resultStr = AppWXPayUtil.requestReverseService(refundReqData);
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
				final String resultStr = AppWXPayUtil.requestCloseService(closeReqData);
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
	public static final AppWXPrePayDTO generateWXPayPrepayId(final Sas sas, final SasMenuGoodOrder order, final List<SasMenuGoodOrderItem> items,
			final SasOrderTransaction transaction, final String openId) {
		return AppWXPayUtil.generateWXPayPrepayId(sas, String.valueOf(order.getId()), transaction, openId);
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
		final ScanPayReqData scanPayReqData = new ScanPayReqData(AppWXPayConstant.getAppID(), AppWXPayConstant.MchID,
				String.valueOf(setting.getId()),
				String.valueOf(transaction.getId()),
				"购买"+setting.getMonthCount() + "个月会员",
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				IOUtil.getLocalIP(),
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/app/wxCallback/result", "JSAPI",
				openId);
		String resultStr = null;
		try {
			resultStr = AppWXPayUtil.requestScanPayService(scanPayReqData);
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
		return new ScanPayService(AppWXPayConstant.CertPassword, AppWXPayUtil.class.getResource("/wxpay-app/apiclient_cert.p12").getPath()).request(scanPayReqData);
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
		return new ScanPayQueryService(AppWXPayConstant.CertPassword, AppWXPayUtil.class.getResource("/wxpay-app/apiclient_cert.p12").getPath()).request(scanPayQueryReqData);
	}
	
    /**
     * 请求退款服务
     * @param refundReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
    public static String requestRefundService(RefundReqData refundReqData) throws Exception{
        return new RefundService(AppWXPayConstant.CertPassword, AppWXPayUtil.class.getResource("/wxpay-app/apiclient_cert.p12").getPath()).request(refundReqData);
    }

    /**
     * 请求退款查询服务
     * @param refundQueryReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
	public static String requestRefundQueryService(RefundQueryReqData refundQueryReqData) throws Exception{
		return new RefundQueryService(AppWXPayConstant.CertPassword, AppWXPayUtil.class.getResource("/wxpay-app/apiclient_cert.p12").getPath()).request(refundQueryReqData);
	}

	/**
     * 请求撤销服务
     * @param reverseReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
	public static String requestReverseService(ReverseReqData reverseReqData) throws Exception{
		return new ReverseService(AppWXPayConstant.CertPassword, AppWXPayUtil.class.getResource("/wxpay-app/apiclient_cert.p12").getPath()).request(reverseReqData);
	}

	/**
     * 请求关闭订单服务
     * @param reverseReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
	public static String requestCloseService(CloseReqData reqData) throws Exception{
		return new CloseService(AppWXPayConstant.CertPassword, AppWXPayUtil.class.getResource("/wxpay-app/apiclient_cert.p12").getPath()).request(reqData);
	}

	public static final void main(String[] args)
	{
		final RefundReqData refundReqData = new RefundReqData(AppWXPayConstant.getAppID(), AppWXPayConstant.MchID, null, 
				"1025929",
				"deviceInof",
				"1002",
				100,
				100, // 微信的单位是分
				"CNY",
				"123");
		String resultStr = null;
		try {
			resultStr = AppWXPayUtil.requestRefundService(refundReqData);
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