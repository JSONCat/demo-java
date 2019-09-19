/**
 * 
 */
package com.sas.core.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.AlipayConstant;
import com.sas.core.constant.AlipayConstant.AliPayUrl;
import com.sas.core.constant.AlipayConstant.AlipayProduct;
import com.sas.core.constant.AlipayConstant.AlipaySignType;
import com.sas.core.constant.AlipayConstant.QRPayMode;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.DomainConstant.SpecialDomain;
import com.sas.core.constant.OrderConstant.TransactionOperation;
import com.sas.core.constant.SasConstant;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.constant.TransactionConstant.OrderGoodType;
import com.sas.core.constant.TransactionConstant.OrderPayType;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.meta.PortalSite;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasCreditPointSetting;
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
import com.sas.core.meta.User;
import com.sas.core.util.alipay.AlipayUtil;
import com.sas.core.util.meta.SasGoodUtil;
import com.sas.core.util.meta.SasUtil;
import com.sas.core.util.wxpay.RandomStringGenerator;
import com.sas.core.util.wxpay.WXPayConstant;
import com.sas.core.util.wxpay.WXPayUtil;
import com.sas.core.util.wxpay.meta.ScanPayReqData;

/**
 * 交易相关的util
 * @author zhuliming
 *
 */
public class TransactionUtil {
	
	public static final Logger logger = Logger.getLogger("com.logger.order");
	
	/**********
	 * 记录订单的日志信息
	 * @param msg
	 */
	public static final void logRefundOrderMessage(final String state, final long orderId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[Refund-" + state + "][o#" + orderId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[Refund-" + state + "][o#" + orderId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}
	
	public static final void logActivityOrderMessage(final String state, final long orderId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[Act-" + state + "][o#" + orderId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[Act-" + state + "][o#" + orderId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}

	public static final void logGoodOrderMessage(final String state, final long orderId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[Good-" + state + "][o#" + orderId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[Good-" + state + "][o#" + orderId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}
	
	public static final void logSmsOrderMessage(final String state, final long orderId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[Sms-" + state + "][o#" + orderId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[Sms-" + state + "][o#" + orderId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}
	
	public static final void logBargainActivityOrderMessage(final String state, final long orderId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[BargainAct-" + state + "][o#" + orderId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[BargainAct-" + state + "][o#" + orderId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}
	
	public static final void logCheapGroupActivityOrderMessage(final String state, final long orderId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[CheapGroupAct-" + state + "][o#" + orderId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[CheapGroupAct-" + state + "][o#" + orderId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}
	
	public static final void logMemberOrderMessage(final String state, final long userId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[Member-" + state + "][o#" + userId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[Member-" + state + "][o#" + userId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}
	
	public static final void logOrderMessage(final String state, final long orderId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[" + state + "][o#" + orderId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[" + state + "][o#" + orderId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}
	
	public static final void logOrderMessage(final String state, final Throwable e){
		logger.error("[" + state + "]" , e);
	}
	
	/*************
	 * 是否是测试环境的交易id
	 * @param transactionId
	 * @return
	 */
	public static final boolean isTestTransactionId(final long transactionId){
		return transactionId < 1000000;
	}
	
	 /******************
	  * 生成支付链接
	  * @param type
	  * @param transaction
	  * @return
	  */
	 public static final String generatePayURL(final OrderPayType type, final Sas sas, final PortalSite portalSite, 
			 final SasOrderTransaction transaction, final long userId, final boolean isBieZhailaActivity)
	 {
		 if(type == null || type == OrderPayType.AliPayDirectPay){
			 return AliPayUrl.HttpsPayUrl.url + AlipayUtil.generateAliPayDirectPayURL(sas, portalSite, transaction, userId, isBieZhailaActivity);
		 }
		 return null;
	 }
	 
	 public static final String generateAliPayDirectPayQRCodeURL(final OrderPayType type, final Sas sas, final PortalSite portalSite, 
			 final SasOrderTransaction transaction, final long userId, final boolean isBieZhailaActivity)
	 {
		 if(type == null || type == OrderPayType.AliPayDirectPay){
			 return AliPayUrl.HttpsPayUrl.url + AlipayUtil.generateAliPayDirectPayQRCodeURL(sas, portalSite, transaction, userId, isBieZhailaActivity);
		 }
		 return null;
	 }
	 
	 /*************
	  * 生成商品，活动的wap支付链接，调整到wap版本
	  * @param type
	  * @param sas
	  * @param portalSite
	  * @param transaction
	  * @param userId
	  * @param isBieZhailaActivity
	  * @return
	  */
	 public static final String generateWapPayURL(final OrderPayType type, final Sas sas, final PortalSite portalSite, final SasOrderTransaction transaction,
			 final long userId, final boolean isBieZhailaActivity)
	 {
		 if(type == null || type == OrderPayType.AliPayDirectPay){
			 return AliPayUrl.HttpsPayUrl.url + AlipayUtil.generateWapAliPayDirectPayURL(sas, portalSite, transaction, userId, isBieZhailaActivity);
		 }
		 return null;
	 }
	 
	 /*************
	  * 生成短信充值链接
	  * @param sas
	  * @param transaction
	  * @return
	  */
	 public static final String generateInsurranceOrderPayURL(final Sas sas, final SasOrderTransaction transaction)
	 {
		 final String supportDomain = "http://" + TransactionUtil.generateAlipaySupportDomain(sas);
		 final String subject = AlipayUtil.removeInvalidURLParam(transaction.getOrderGoodName(), 85);
		 Map<String, String> allParamMap = new HashMap<String, String>();
		 allParamMap.put("service", AlipayProduct.CreateDirectPayByUser.pName);
		 allParamMap.put("partner", AlipayConstant.getPartner()); 
		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
		 allParamMap.put("notify_url", supportDomain + AliPayUrl.AlipayCallbackAsynchronize.url);
		 allParamMap.put("return_url", supportDomain + AliPayUrl.AlipayCallbackSynchronize.url);
		 allParamMap.put("out_trade_no", String.valueOf(transaction.getId()));
		 allParamMap.put("subject", subject);
		 allParamMap.put("payment_type", transaction.getThirdpartPaymentType());	
		 allParamMap.put("seller_email", SasConstant.SaihuitongAlipayAccount); 
		 allParamMap.put("total_fee", transaction.getUnitPrice().setScale(2, BigDecimal.ROUND_HALF_UP).toString()); //分变成了元
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", transaction.getOrderGoodUrl());
		 }
		 allParamMap.put("it_b_pay", AlipayUtil.generateTransactionExpireTime(transaction.getExpireTime()).getKey()); //每次动态调整		 
		 allParamMap.put("qr_pay_mode", QRPayMode.JumpMode.mode);
		 
		 //再做一层md5， 防止第三方的恶意回调
//		 allParamMap.put("extra_common_param", new OnlinePayExtraParam(userId, null, true, false).toString());			
		 //生成签名  	
		 allParamMap = AlipayUtil.generateAllParamsWithSignature(sas, allParamMap, AlipaySignType.MD5, false);
		 
		 //因为宝贝名字是中文，进行encode
		 allParamMap.put("subject", HtmlUtil.encodeParam(subject, Encoding.UTF8));
		 return AliPayUrl.HttpsPayUrl.url + AlipayUtil.generateRequestQueryString(allParamMap);
	 }
	 
	 /*************
	  * 生成短信充值链接
	  * @param sas
	  * @param transaction
	  * @return
	  */
	 public static final String generateSmsChargePayURL(final Sas sas, final long userId, final SasOrderTransaction transaction)
	 {
		 final String supportDomain = "http://" + TransactionUtil.generateAlipaySupportDomain(sas);
		 final String subject = AlipayUtil.removeInvalidURLParam(transaction.getOrderGoodName(), 85);
		 Map<String, String> allParamMap = new HashMap<String, String>();
		 allParamMap.put("service", AlipayProduct.CreateDirectPayByUser.pName);
		 allParamMap.put("partner", AlipayConstant.getPartner()); 
		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
		 allParamMap.put("notify_url", supportDomain + AliPayUrl.AlipayCallbackAsynchronize.url);
		 allParamMap.put("return_url", supportDomain + AliPayUrl.AlipayCallbackSynchronize.url);
		 allParamMap.put("out_trade_no", String.valueOf(transaction.getId()));
		 allParamMap.put("subject", subject);
		 allParamMap.put("payment_type", transaction.getThirdpartPaymentType());	
		 allParamMap.put("seller_email", SasConstant.SaihuitongAlipayAccount); 
		 allParamMap.put("total_fee", transaction.getUnitPrice().divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString()); //分变成了元
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", transaction.getOrderGoodUrl());
		 }
		 allParamMap.put("it_b_pay", AlipayUtil.generateTransactionExpireTime(transaction.getExpireTime()).getKey()); //每次动态调整		 
		 allParamMap.put("qr_pay_mode", QRPayMode.JumpMode.mode);
		 		
		 //生成签名  	
		 allParamMap = AlipayUtil.generateAllParamsWithSignature(sas, allParamMap, AlipaySignType.MD5, false);
		 
		 //因为宝贝名字是中文，进行encode
		 allParamMap.put("subject", HtmlUtil.encodeParam(subject, Encoding.UTF8));
		 return AliPayUrl.HttpsPayUrl.url + AlipayUtil.generateRequestQueryString(allParamMap);
	 }

	 /*************
	  * 生成网站充值链接
	  * @param sas
	  * @param transaction
	  * @return
	  */
	 public static final String generateSasRechargePayURL(final Sas sas, final long userId, final SasOrderTransaction transaction)
	 {
		 final String supportDomain = "http://" + TransactionUtil.generateAlipaySupportDomain(sas);
		 final String subject = AlipayUtil.removeInvalidURLParam(transaction.getOrderGoodName(), 85);
		 Map<String, String> allParamMap = new HashMap<String, String>();
		 allParamMap.put("service", AlipayProduct.CreateDirectPayByUser.pName);
		 allParamMap.put("partner", AlipayConstant.getPartner()); 
		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
		 allParamMap.put("notify_url", supportDomain + AliPayUrl.AlipayCallbackAsynchronize.url);
		 allParamMap.put("return_url", supportDomain + AliPayUrl.AlipayCallbackSynchronize.url);
		 allParamMap.put("out_trade_no", String.valueOf(transaction.getId()));
		 allParamMap.put("subject", subject);
		 allParamMap.put("payment_type", transaction.getThirdpartPaymentType());	
		 allParamMap.put("seller_email", SasConstant.SaihuitongAlipayAccount); 
		 allParamMap.put("total_fee", transaction.getUnitPrice().setScale(2, BigDecimal.ROUND_HALF_UP).toString()); 
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", transaction.getOrderGoodUrl());
		 }
		 allParamMap.put("it_b_pay", AlipayUtil.generateTransactionExpireTime(transaction.getExpireTime()).getKey()); //每次动态调整		 
		 allParamMap.put("qr_pay_mode", QRPayMode.JumpMode.mode);
		 		
		 //生成签名  	
		 allParamMap = AlipayUtil.generateAllParamsWithSignature(sas, allParamMap, AlipaySignType.MD5, false);
		 
		 //因为宝贝名字是中文，进行encode
		 allParamMap.put("subject", HtmlUtil.encodeParam(subject, Encoding.UTF8));
		 return AliPayUrl.HttpsPayUrl.url + AlipayUtil.generateRequestQueryString(allParamMap);		 
	 }
	 
	 /**********
	  * 生成退款链接
	  * @param sas
	  * @param type
	  * @param transaction
	  * @return
	  */
	 public static final String generateRefundPayURL(final OrderPayType type, final Sas sas, final PortalSite portalSite, final SasOrderTransaction transaction, final User admin){
		 if(type == null || type == OrderPayType.AliPayDirectPay){
			 return AliPayUrl.HttpsPayUrl.url + AlipayUtil.generateRefundAliPayDirectPayURL(sas, portalSite, transaction, admin);
		 }
		 return null;
	 }

	 /***************
	  * 生成支持支付的域名
	  * @param sas
	  * @return
	  */
	 public static final String generateAlipaySupportDomain(final Sas sas){
		 final String subDomain = sas.getSubDomain();
		 if(subDomain.endsWith("."+SpecialDomain.paobu_com.domain)){
			 return "www." + SpecialDomain.saihuitong_com.domain; //return subDomain;
		 }else if(subDomain.endsWith("."+SpecialDomain.saihuitong_com.domain)){
			 return subDomain;
		 }else{
			 return "www." + SpecialDomain.saihuitong_com.domain;
		 }	 
	 }
	 
	 /*********************
	  * 读取request中的参数列表
	  * @param request
	  * @param paramNames
	  * @return
	  */
	 public static final Map<String, String> readAllParameters(final HttpServletRequest request)
	 {
		final Map<String,String> params = new HashMap<String,String>();
		final Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			final String name =  iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			if(StringUtils.isNotBlank(valueStr))
			{
				if("subject".equals(name) || "body".equals(name))
				{
					valueStr = HtmlUtil.decodeParam(valueStr, Encoding.UTF8, valueStr);
					if(!TransactionOperation.isStartTheseWords(valueStr)){//是否已经转换正常
						valueStr = HtmlUtil.convertParamEncoding(valueStr, Encoding.ISO88591,
								Encoding.UTF8);
					}
					params.put(name, valueStr);
				 }else{
					 params.put(name, valueStr);
				 }
			 }
		}
		return params;
	 }
	 
	 /****************
	  * 计算交易价格
	  * @param transaction
	  * @return
	  */
	 public static final BigDecimal calculateTotalTransactionPrice(final List<SasMenuActivityOrder> activityOrders, final SasMenuGoodOrder goodsOrder){
		 BigDecimal total = new BigDecimal(0);
		 if(CollectionUtils.isNotEmpty(activityOrders)){
			 for(final SasMenuActivityOrder order : activityOrders)
			 {
				 total = total.add(order.getNeedPayTotalPrice());
			 }
		 }
		 if(goodsOrder != null){
			 total = total.add(goodsOrder.getTotalPriceWithRefundAmount());
		 }
		 return total.setScale(2, BigDecimal.ROUND_HALF_UP);
	 }
	 
	 public static final BigDecimal calculateTotalTransactionPrice(final SasOrderTransaction transaction){
		if(transaction == null){
			return new BigDecimal(0);
		}
		BigDecimal r = null;
		final int type = transaction.getOrderGoodType();
		if(type == OrderGoodType.ActivityRefund.type 
				|| type == OrderGoodType.GoodRefund.type
				|| type == OrderGoodType.MixActivityGoods.type){
			r = transaction.getChangePrice();
		}else{
			r = transaction.getUnitPrice().multiply(transaction.getGoodCount()).add(transaction.getChangePrice())
				.add(transaction.getAdditionalServicePrice());
		}		
		return r.setScale(2, BigDecimal.ROUND_HALF_UP);
	 }
	 
	
	 /*********************
	  * 根据传递的参数， 设置回调值， 如果没有更新， 则返回false
	  * @param transactionFromDatabase
	  * @param transactionFromAlipay
	  * @return
	  */
	 public static final boolean setTransactionCallbackParameters(final SasOrderTransaction transactionFromDatabase, final SasOrderTransaction transactionFromAlipay)
	 {
		 if(transactionFromDatabase == null){
			 return false;
		 }
		 boolean changed = false;
		 //thirdpartTransactionId
		 if(StringUtils.isBlank(transactionFromDatabase.getThirdpartTransactionId())
				 && StringUtils.isNotBlank(transactionFromAlipay.getThirdpartTransactionId())){
			 transactionFromDatabase.setThirdpartTransactionId(transactionFromAlipay.getThirdpartTransactionId());
			 changed = true;			 
		 }
		 //callbackTotalPrice
		 if((transactionFromDatabase.getCallbackTotalPrice() == null || transactionFromDatabase.getCallbackTotalPrice().doubleValue() < 0.01)
				 && transactionFromAlipay.getCallbackTotalPrice() != null && transactionFromAlipay.getCallbackTotalPrice().doubleValue() > 0){
			 transactionFromDatabase.setCallbackTotalPrice(transactionFromAlipay.getCallbackTotalPrice());
			 changed = true;			 
		 }
		 //callbackNotifyId
		 if(StringUtils.isBlank(transactionFromDatabase.getCallbackNotifyId())
				 && StringUtils.isNotBlank(transactionFromAlipay.getCallbackNotifyId())){
			 transactionFromDatabase.setCallbackNotifyId(transactionFromAlipay.getCallbackNotifyId());
			 changed = true;			 
		 }
		 //callbackBuyerAccountName
		 if(StringUtils.isBlank(transactionFromDatabase.getCallbackBuyerAccountName())
				 && StringUtils.isNotBlank(transactionFromAlipay.getCallbackBuyerAccountName())){
			 transactionFromDatabase.setCallbackBuyerAccountName(transactionFromAlipay.getCallbackBuyerAccountName());
			 changed = true;			 
		 }
		 //callbackState and state
		 if(StringUtils.isBlank(transactionFromDatabase.getCallbackState())
				 && StringUtils.isNotBlank(transactionFromAlipay.getCallbackState())){
			 transactionFromDatabase.setCallbackState(transactionFromAlipay.getCallbackState());
			 changed = true;			 
		 }
		 //callbackTime
		 transactionFromDatabase.setCallbackTime(transactionFromAlipay.getCallbackTime());
		 return changed;
	 }
	 
	 /************
	  * 生成会员升级续费的套餐名
	  * @param name
	  * @param setting
	  * @return
	  */
	 public static final String generateMemeberUpgradeGoodName(String name, SasMemberUpgradeSetting setting)
	 {
		name = name + "会员升级续费";
		if(name.length() >= 32){
			name = "会员升级续费";
		}
		String tail = null;
		if(setting.getMonthCount() < 1){
			tail = "(永久有效)";
		} else if(setting.getMonthCount() >= 12){
			tail = (setting.getMonthCount() % 12 == 0) ? "(" + setting.getMonthCount()/12 + "年)"
					: "(" + setting.getMonthCount()/12 + "年" + setting.getMonthCount()%12 + "个月)";
		} else{
			tail = "(" + setting.getMonthCount() + "个月)";
		}
		name = name + tail;
		return name.length() >= 32 ? name : "会员升级续费" + tail;	
	}
	 
	 /***********
	  * 转成微信支付需要的参数信息
	  * @param sas
	  * @param good
	  * @param order
	  * @param transaction
	  * @param openId
	  * @return
	  */
	public static final Map<String, Object> convertGoodOrderTransactionParams(final Sas sas, 
			final SasMenuGoodOrder order, final List<SasMenuGoodOrderItem> items, final SasOrderTransaction transaction,
			final String openId) {
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				String.valueOf(order.getId()),
				String.valueOf(transaction.getId()),
				WXPayUtil.generateWXPayGoodName(SasGoodUtil.createOrderGoodNames("购买", items)),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				"183.129.211.34",
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "NATIVE",
				"");
		return TransactionUtil.convertOrderTransactionParams(scanPayReqData, transaction);
	}
	
	/**
	 * 转成微信支付需要的参数信息
	 * @Date: Aug 17, 2015
	 * @Time: 11:02:00 AM
	 * @param sasOrderTransaction
	 * @param sasMenuActivityOrder
	 * @return
	 */
	public static final Map<String, Object> convertMixActivityGoodOrderTransactionParams(final SasOrderTransaction transaction, final String openId) {
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				String.valueOf(transaction.getId()),
				String.valueOf(transaction.getId()),
				HtmlUtil.subStringWhenAccessLimit(transaction.getOrderGoodName(), 32),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				"183.129.211.34",
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "NATIVE",
				"");
		return TransactionUtil.convertOrderTransactionParams(scanPayReqData, transaction);
	}
	
	/**
	 * 转成微信支付需要的参数信息
	 * @Date: Aug 17, 2015
	 * @Time: 11:02:00 AM
	 * @param sasOrderTransaction
	 * @param sasMenuActivityOrder
	 * @return
	 */
	public static final Map<String, Object> convertActivityOrderTransactionParams(final Sas sas, final SasMenuActivity activity, final SasMenuActivityOrder order,
			final SasOrderTransaction transaction, final String openId) {
		  final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				String.valueOf(order.getId()),
				String.valueOf(transaction.getId()),
				WXPayUtil.generateWXPayGoodName(TransactionOperation.ActivityApply.name + activity.getTitle()),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				"183.129.211.34",
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "NATIVE",
				"");
		  return TransactionUtil.convertOrderTransactionParams(scanPayReqData, transaction);
	}
		
	/**
	 * 转成微信支付需要的参数信息
	 * @Date: Aug 17, 2015
	 * @Time: 11:02:00 AM
	 * @param sasOrderTransaction
	 * @param sasMenuActivityOrder
	 * @return
	 */
	public static final Map<String, Object> convertActivityOrderTransactionParams(final Sas sas,
			final SasMarketBargainActivity activity, final SasMarketBargainPerson person,
			final SasOrderTransaction transaction, final String openId) {
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				String.valueOf(person.getId()),
				String.valueOf(transaction.getId()),
				WXPayUtil.generateWXPayGoodName(TransactionOperation.MarketBargainActivityJoin.name + activity.getTitle()),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				"183.129.211.34",
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "NATIVE",
				"");
		return TransactionUtil.convertOrderTransactionParams(scanPayReqData, transaction);
	}
	
	/**
	 * 转成微信支付需要的参数信息
	 * @Date: Aug 17, 2015
	 * @Time: 11:02:00 AM
	 * @param sasOrderTransaction
	 * @param sasMenuActivityOrder
	 * @return
	 */
	public static final Map<String, Object> convertActivityOrderTransactionParams(final Sas sas,
			final SasMarketGroupCheapActivity activity, final SasMarketGroupCheapPerson person,
			final SasOrderTransaction transaction, final String openId) {
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				String.valueOf(person.getId()),
				String.valueOf(transaction.getId()),
				WXPayUtil.generateWXPayGoodName(TransactionOperation.MarketCheapGroupCheapActivityJoin.name + activity.getTitle()),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				"183.129.211.34",
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "NATIVE",
				"");
		return TransactionUtil.convertOrderTransactionParams(scanPayReqData, transaction);
	}
	
	 /***********
	  * 转成微信支付需要的参数信息
	  * @param sas
	  * @param good
	  * @param order
	  * @param transaction
	  * @param openId
	  * @return
	  */
	public static final Map<String, Object> convertMemberUpgradeOrderTransactionParams(final Sas sas, final SasMemberUpgradeSetting setting,
		final SasOrderTransaction transaction, final String openId) {
		final ScanPayReqData scanPayReqData = new ScanPayReqData(WXPayConstant.getAppID(), WXPayConstant.MchID,
				String.valueOf(setting.getId()),
				String.valueOf(transaction.getId()),
				TransactionUtil.generateMemeberUpgradeGoodName(sas.getName(), setting),
				TransactionUtil.calculateTotalTransactionPrice(transaction).multiply(new BigDecimal(100)).intValue(), // 微信的单位是分
				"183.129.211.34",
				TimeUtil.formatDate(transaction.getCreateTime(), TimeFormat.YYYYMMDDHHMMSS),
				TimeUtil.formatDate(transaction.getExpireTime(), TimeFormat.YYYYMMDDHHMMSS),
				String.valueOf(transaction.getChangePrice()),
				WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result", "NATIVE",
				"");
		return TransactionUtil.convertOrderTransactionParams(scanPayReqData, transaction);
	}
	
	private static final Map<String, Object> convertOrderTransactionParams(final ScanPayReqData scanPayReqData, final SasOrderTransaction transaction) { 
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", WXPayConstant.getAppID());
		params.put("mch_id", WXPayConstant.MchID);
		params.put("nonce_str", RandomStringGenerator.getRandomStringByLength(32));
		params.put("sign", scanPayReqData.getSign());
		params.put("body", transaction.getOrderGoodName());
		params.put("out_trade_no", transaction.getOrderId());
		params.put("total_fee", TransactionUtil.calculateTotalTransactionPrice(transaction).toString());
		params.put("spbill_create_ip", "NativeIP");
		params.put("notify_url", WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result");
		params.put("trade_type", "JSAPI");
		return params;
	}
  
  /**
   * 转化为微信H5支付需要的参数
   *
   * @param scanPayReqData
   * @param transaction
   * @return
   */
	private static final Map<String, Object> convertWxH5PayOrderTransactionParams(final ScanPayReqData scanPayReqData, final SasOrderTransaction transaction){
	  Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", WXPayConstant.getAppID());
		params.put("mch_id", WXPayConstant.MchID);
		params.put("nonce_str", RandomStringGenerator.getRandomStringByLength(32));
		params.put("sign", scanPayReqData.getSign());
		params.put("body", transaction.getOrderGoodName());
		params.put("out_trade_no", transaction.getOrderId());
		params.put("total_fee", TransactionUtil.calculateTotalTransactionPrice(transaction).toString());
		params.put("spbill_create_ip", "183.159.172.11");
		params.put("notify_url", WXPayConstant.WxPayHTTPProtocol + "www.saihuitong.com/wxCallback/result");
		params.put("trade_type", "MWEB");
		params.put("scene_info", "{'h5_info':{'type':'Wap','wap_url':'http://www.saihuitong.com','wap_name':'赛会通'}}");
		return params;
  }
	
	/**************
	 * 计算能用的积分的最大积分和可以抵扣就得钱
	 * @param userCreditPointBalance
	 * @param maxDeductMoney
	 * @param ratioSetting
	 * @return
	 */
	public static final BinaryEntry<Integer, BigDecimal> calculateActivityMaxCreditPointMoney(final long userCreditPointBalance, 
			final BigDecimal maxDeductMoney,  final SasCreditPointSetting ratioSetting){
		final BinaryEntry<Integer, BigDecimal> result = TransactionUtil.calculateMaxCreditPointMoney(userCreditPointBalance, maxDeductMoney, ratioSetting);
		if(result.value != null){//四舍五入计算整数
			result.value = result.value.setScale(0, BigDecimal.ROUND_DOWN);
			if(result.value.intValue() < 1){
				return new BinaryEntry<Integer, BigDecimal>(0, new BigDecimal(0));
			}else{//需要减去小数多扣除的积分
				result.key = result.value.multiply(new BigDecimal(ratioSetting.getCount()))
						.divide(ratioSetting.getMoney(), 2, BigDecimal.ROUND_DOWN)
						.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();;
				return result;
			}			
		}
		return new BinaryEntry<Integer, BigDecimal>(0, new BigDecimal(0));
	}
			
	public static final BinaryEntry<Integer, BigDecimal> calculateMaxCreditPointMoney(final long userCreditPointBalance, 
			final BigDecimal maxDeductMoney,  final SasCreditPointSetting ratioSetting)
	{
		if(userCreditPointBalance < 1 || maxDeductMoney.doubleValue() < 0.01 || ratioSetting.getCount() < 1 
				|| ratioSetting.getMoney().doubleValue() < 0.01){
			return new BinaryEntry<Integer, BigDecimal>(0, new BigDecimal(0));
		}
		final int maxUsePoints = maxDeductMoney.multiply(new BigDecimal(ratioSetting.getCount()))
				.divide(ratioSetting.getMoney(), 2, BigDecimal.ROUND_DOWN)
				.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
		if(maxUsePoints <= userCreditPointBalance){
			if(maxUsePoints < 1){
				return new BinaryEntry<Integer, BigDecimal>(0, new BigDecimal(0));
			}
			return new BinaryEntry<Integer, BigDecimal>(maxUsePoints, maxDeductMoney);
		}
		return TransactionUtil.calculateMaxCreditPointMoney((int)(userCreditPointBalance), ratioSetting);
	}
	
	public static final BinaryEntry<Integer, BigDecimal> calculateMaxCreditPointMoney(final BigDecimal maxDeductMoney, 
			final SasCreditPointSetting ratioSetting)
	{
		if(maxDeductMoney.doubleValue() < 0.01 || ratioSetting.getCount() < 1
				|| ratioSetting.getMoney().doubleValue() < 0.01){
			return new BinaryEntry<Integer, BigDecimal>(0, new BigDecimal(0));
		}
		final int maxUsePoints = maxDeductMoney.multiply(new BigDecimal(ratioSetting.getCount()))
				.divide(ratioSetting.getMoney(), 2, BigDecimal.ROUND_DOWN)
				.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
		return new BinaryEntry<Integer, BigDecimal>(maxUsePoints, maxDeductMoney); 
	}	
	
	/************
	 * 把钱换成整数，然后再计算相应的抵扣积分取整，做到四舍五入，不包含小数
	 * @param maxUsePoints
	 * @param maxDeductMoney
	 * @param ratioSetting
	 * @return
	 */
	private static final BinaryEntry<Integer, BigDecimal> calculateMaxCreditPointMoney(int maxUsePoints, 
			final SasCreditPointSetting ratioSetting){
		if(maxUsePoints > 0 && ratioSetting.getMoney().doubleValue() >= 0.01 && ratioSetting.getCount() > 0){
			final BigDecimal maxUsePointsMoney = ratioSetting.getMoney().multiply(new BigDecimal(maxUsePoints))
					.divide(new BigDecimal(ratioSetting.getCount()), 2, BigDecimal.ROUND_DOWN); //算出能抵扣的钱
			return  new BinaryEntry<Integer, BigDecimal>(maxUsePoints, maxUsePointsMoney);
		}
		return new BinaryEntry<Integer, BigDecimal>(0, new BigDecimal(0));
	}
	
	/************
	 * 根据价格等进行计算
	 * @param userCreditPointBalance
	 * @param maxDeductMoney
	 * @param ratioSetting
	 * @param totalNeedPayPrice
	 * @return
	 */
	public static final BinaryEntry<Integer, BigDecimal> calculateMaxUsableCreditPointMoney(final long userCreditPointBalance, 
			final BigDecimal maxDeductMoney,  final SasCreditPointSetting ratioSetting, final BigDecimal totalNeedPayPrice)
	{
		if(userCreditPointBalance < 1 || maxDeductMoney.doubleValue() < 0.01 || ratioSetting.getCount() < 1
				|| ratioSetting.getMoney().doubleValue() < 0.01
				|| totalNeedPayPrice.doubleValue() < 0.01){
			return new BinaryEntry<Integer, BigDecimal>(0, new BigDecimal(0));
		}
		if(maxDeductMoney.doubleValue() < totalNeedPayPrice.doubleValue()){
			return calculateMaxCreditPointMoney(userCreditPointBalance, maxDeductMoney, ratioSetting);
		}else{
			return calculateMaxCreditPointMoney(userCreditPointBalance, totalNeedPayPrice, ratioSetting);
		}
	}
	
	public static final BinaryEntry<Integer, BigDecimal> calculateActivityMaxUsableCreditPointMoney(final long userCreditPointBalance, 
			final BigDecimal maxDeductMoney,  final SasCreditPointSetting ratioSetting, final double totalNeedPayPrice)
	{
		if(userCreditPointBalance < 1 || maxDeductMoney.doubleValue() < 0.01 || ratioSetting.getCount() < 1
				|| ratioSetting.getMoney().doubleValue() < 0.01
				|| totalNeedPayPrice < 0.01){
			return new BinaryEntry<Integer, BigDecimal>(0, new BigDecimal(0));
		}
		BinaryEntry<Integer, BigDecimal> result = null;
		if(maxDeductMoney.doubleValue() < totalNeedPayPrice){
			result = calculateActivityMaxCreditPointMoney(userCreditPointBalance, maxDeductMoney, ratioSetting);
		}else{
			result = calculateActivityMaxCreditPointMoney(userCreditPointBalance, new BigDecimal(totalNeedPayPrice), ratioSetting);
		}
		return result; 
	}

	/*************
	 * 该笔交易是否需要返还佣金
	 * 只有通过赛会通交易的线上交易佣金才需要退还佣金
	 * @param sas
	 * @param orderPayType
	 * @return
	 */
	public static final boolean needRebateTransactionCommission(final Sas sas, final int orderPayType)
	{
		return !(OrderPayType.AliPayDirectPay.type == orderPayType && SasUtil.isSasSupportAlipay(sas))
				&& !OrderPayType.isOfflinePayOrOtherPay(orderPayType);
	}
}
