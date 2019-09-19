/**
 * 
 */
package com.sas.core.util.alipay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.sas.core.constant.AlipayConstant;
import com.sas.core.constant.AlipayConstant.AliPayUrl;
import com.sas.core.constant.AlipayConstant.AlipayKey;
import com.sas.core.constant.AlipayConstant.AlipayProduct;
import com.sas.core.constant.AlipayConstant.AlipaySignType;
import com.sas.core.constant.AlipayConstant.QRPayMode;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.DomainConstant;
import com.sas.core.constant.SasConstant;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.exception.ServerUnknownException;
import com.sas.core.meta.PortalSite;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasMenuActivity;
import com.sas.core.meta.SasMenuActivityOrder;
import com.sas.core.meta.SasOrderTransaction;
import com.sas.core.meta.SasRefundOrder;
import com.sas.core.meta.User;
import com.sas.core.util.HtmlUtil;
import com.sas.core.util.IdUtil;
import com.sas.core.util.TimeUtil;
import com.sas.core.util.TransactionUtil;
import com.sas.core.util.meta.SasUtil;

/**
 * 阿里支付相关的util
 * @author zhuliming
 *
 */
public class AlipayUtil {

	 public static final Logger logger = Logger.getLogger(AlipayUtil.class);
	 
	 /**************
	  *设置未付款交易的超时时间，一旦超时，该笔交易就会自动被关闭。		
	  *取值范围：1m～15d。		m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。		
	  *该参数数值不接受小数点，如1.5h，可转换为90m。
	  *该功能需要联系支付宝配置关闭时间。
	  */
	 public static final SasOrderTransaction setTransactionExpireTime(final SasMenuActivity activity,
				final SasMenuActivityOrder order, final SasOrderTransaction transaction){
		 BinaryEntry<String, Long> expireData = AlipayUtil.generateTransactionExpireTime(activity.getEndTime());
		 if(expireData == null){
			 expireData = new BinaryEntry<String, Long>("7d", System.currentTimeMillis() + 7 * Miliseconds.OneDay.miliseconds); 
		 }
		 transaction.setLifeCycle(expireData.getKey());
		 transaction.setExpireTime(expireData.getValue());
		 return transaction;
	 }
	 
	 public static final SasOrderTransaction setDefaultTransactionExpireTime(final SasOrderTransaction transaction){
		 transaction.setLifeCycle("15d");
		 transaction.setExpireTime(System.currentTimeMillis() + 15 * Miliseconds.OneDay.miliseconds);
		 return transaction;
	 }
	 
	 public static final SasOrderTransaction setTransactionExpireTime(final int days, 
			 final SasOrderTransaction transaction){
		 transaction.setLifeCycle(days + "d");
		 transaction.setExpireTime(System.currentTimeMillis() + days * Miliseconds.OneDay.miliseconds);
		 return transaction;
	 }
	 
	 public static final BinaryEntry<String, Long> generateTransactionExpireTime(final long endTime)
	 {
		 final long now = System.currentTimeMillis();
		 long hours = (endTime - now) % Miliseconds.OneHour.miliseconds;
//		 int days = (int)(hours / 24);
		 if(hours < 1){
			 int minutes = (int)((endTime - now) % Miliseconds.OneMinute.miliseconds);
			 if(minutes < 1){
				 minutes = 1;
			 }
			 return new BinaryEntry<String, Long>(minutes + "m", now + minutes * Miliseconds.OneMinute.miliseconds);	 
		 }else{
			 return new BinaryEntry<String, Long>("1h", now + Miliseconds.OneHour.miliseconds);
//			 else if(days < 1){
//				 return new BinaryEntry<String, Long>(hours + "h", now + hours * Miliseconds.OneHour.miliseconds);
//			 }else {
//				 if(days > 15){
//					 days = 15;
//				 }
//				 return new BinaryEntry<String, Long>(days + "d", now + days * Miliseconds.OneDay.miliseconds); 
//			 }
		 }
	 }
	 
	 /*****************
	  *  参数body（商品描述）、subject（商品名称）、extra_common_param（公用回传参数）不能包含特殊字符（如：#、%、&、+）、敏感词汇，也不能使用外国文字
	  *  删除特殊字符
	  * @param value
	  * @return
	  */
	 public static final String removeInvalidURLParam(String value, final int maxLength)
	 {
		 if(value == null){
			 return "";
		 }
		 value = value.trim();
		 final StringBuilder sb = new StringBuilder("");
		 for(int i=0; i<value.length(); i++){
			 final char ch = value.charAt(i);
			 if(ch == '#' || ch == '%' || ch == '&' || ch == '+' || ch == '|' || ch == '^' 
					 || ch == '@' || ch == '-' || ch == '【' || ch == '】' || ch == '[' || ch == ']'){
				 continue;
			 }
			 sb.append(ch);
		 }
		final String result = sb.toString().replaceAll(HtmlUtil.WhiteSpaceReg, "");
		if(result.length() > maxLength && maxLength > 0){
			return result.substring(0, maxLength); 
		}
		return result;
	 }
	 
	 /******************
	  * 生成PC版本支付链接
	  * @param type
	  * @param transaction
	  * @return
	  */
	 public static final String generateAliPayDirectPayURL(final Sas sas, final PortalSite portalSite, 
			 final SasOrderTransaction transaction, final long userId, final boolean isBieZhailaActivity)
	 {
		 final boolean isSasSupportAlipay = !isBieZhailaActivity && SasUtil.isSasSupportAlipay(sas);
		 final BigDecimal totalPrice = TransactionUtil.calculateTotalTransactionPrice(transaction);
		 final String supportDomain = "http://" + TransactionUtil.generateAlipaySupportDomain(sas);
		 String subject = AlipayUtil.removeInvalidURLParam(transaction.getOrderGoodName(), 85);
		 Map<String, String> allParamMap = new HashMap<String, String>();
		 allParamMap.put("service", AlipayProduct.CreateDirectPayByUser.pName);
		 allParamMap.put("partner", (isSasSupportAlipay ? sas.getAlipayPartnerId() : AlipayConstant.getPartner())); //走他们自己的支付宝
		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
		 allParamMap.put("notify_url", supportDomain + AliPayUrl.AlipayCallbackAsynchronize.url);
		 allParamMap.put("return_url", supportDomain + AliPayUrl.AlipayCallbackSynchronize.url);
		 allParamMap.put("out_trade_no", String.valueOf(transaction.getId()));
		 allParamMap.put("subject", subject); //生成签名前需要放入,下面生成url前在encode
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", transaction.getOrderGoodUrl()); //生成签名前需要放入,下面生成url前在encode
		 }
		 allParamMap.put("payment_type", transaction.getThirdpartPaymentType());	
		 allParamMap.put("seller_email", (isSasSupportAlipay ? sas.getAlipay() : SasConstant.SaihuitongAlipayAccount)); //走他们自己的支付宝
		 allParamMap.put("total_fee", totalPrice.toString());
		 allParamMap.put("it_b_pay", AlipayUtil.generateTransactionExpireTime(transaction.getExpireTime()).getKey()); //每次动态调整		 
		 allParamMap.put("qr_pay_mode", QRPayMode.JumpMode.mode);
		 
		 //自定义参数
		 allParamMap.put("extra_common_param", transaction.getOrderExtraParam());	//PC端必须要给这个参数，以免PC-》WAP-》支付宝支付PC老订单时， 给用户生成提现记录，导致给站点两笔钱	 
		 /*MD5SignUtil.generateSignature(Md5Salt.AlipayParam,
				 String.valueOf(transaction.getSasId()) + String.valueOf(transaction.getId()), 64));	*/
		 
		 //王斌的意思是，走赛会通的 不再分润 ：由于不支持直接转账给第三方， 只能以分润的形式全额转给第三方：设置分润接口
		 //BinaryEntry<String, String> royaltyParameters = null;
		 //if(SasUtil.isSasSupportAlipay(sas) && !isBieZhailaActivity){
		//	 royaltyParameters = AlipayUtil.createPayRoyaltyParameters(sas, 
		//			 totalPrice, subject, allParamMap);
		//	 if(royaltyParameters != null){
		//		allParamMap.put("royalty_type", royaltyParameters.key);//"10","卖家给第三方提成"
		//		allParamMap.put("royalty_parameters", royaltyParameters.value);
		//	 }
		// }

		 //生成签名  	
		 allParamMap = AlipayUtil.generateAllParamsWithSignature(sas, allParamMap, AlipaySignType.MD5, true);
		 
		 //因为宝贝名字是中文，进行encode
		 allParamMap.put("subject", HtmlUtil.encodeParam(subject, Encoding.UTF8));
		 //if(royaltyParameters != null){
		 //	 allParamMap.put("royalty_parameters", HtmlUtil.encodeParam(royaltyParameters.value, Encoding.UTF8));
		 //}
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", HtmlUtil.encodeParam(transaction.getOrderGoodUrl(), Encoding.UTF8)); //链接可能带有&
		 }
		 return generateRequestQueryString(allParamMap);
	 }
	 
	 /******************
	  * 生成PC版本支付的二维码的iframe链接
	  * @param type
	  * @param transaction
	  * @return
	  */
	 public static final String generateAliPayDirectPayQRCodeURL(final Sas sas, final PortalSite portalSite, 
			 final SasOrderTransaction transaction, final long userId, final boolean isBieZhailaActivity)
	 {
		 final boolean isSasSupportAlipay = !isBieZhailaActivity && SasUtil.isSasSupportAlipay(sas);
		 final BigDecimal totalPrice = TransactionUtil.calculateTotalTransactionPrice(transaction);
		 final String supportDomain = "http://" + TransactionUtil.generateAlipaySupportDomain(sas);
		 String subject = AlipayUtil.removeInvalidURLParam(transaction.getOrderGoodName(), 85);
		 Map<String, String> allParamMap = new HashMap<String, String>();
		 allParamMap.put("service", AlipayProduct.CreateDirectPayByUser.pName);
		 allParamMap.put("partner", (isSasSupportAlipay ? sas.getAlipayPartnerId() : AlipayConstant.getPartner())); //走他们自己的支付宝
		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
		 allParamMap.put("notify_url", supportDomain + AliPayUrl.AlipayCallbackAsynchronize.url);
		 //allParamMap.put("return_url", supportDomain + AliPayUrl.AlipayCallbackSynchronize.url);
		 allParamMap.put("out_trade_no", String.valueOf(transaction.getId()));
		 allParamMap.put("subject", subject); //生成签名前需要放入,下面生成url前在encode
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", transaction.getOrderGoodUrl()); //生成签名前需要放入,下面生成url前在encode
		 }
		 allParamMap.put("payment_type", transaction.getThirdpartPaymentType());	
		 allParamMap.put("seller_email", (isSasSupportAlipay ? sas.getAlipay() : SasConstant.SaihuitongAlipayAccount)); //走他们自己的支付宝
		 allParamMap.put("total_fee", totalPrice.toString());
		 allParamMap.put("it_b_pay", AlipayUtil.generateTransactionExpireTime(transaction.getExpireTime()).getKey()); //每次动态调整		 
		 allParamMap.put("qr_pay_mode", QRPayMode.QRCodeOnly.mode);
		 allParamMap.put("qrcode_width", "270");
		 //自定义参数
		 allParamMap.put("extra_common_param", transaction.getOrderExtraParam());	//PC端必须要给这个参数，以免PC-》WAP-》支付宝支付PC老订单时， 给用户生成提现记录，导致给站点两笔钱	 
		 /*MD5SignUtil.generateSignature(Md5Salt.AlipayParam,
				 String.valueOf(transaction.getSasId()) + String.valueOf(transaction.getId()), 64));	*/
		 
		 //王斌的意思是，走赛会通的 不再分润 ：由于不支持直接转账给第三方， 只能以分润的形式全额转给第三方：设置分润接口
//		 BinaryEntry<String, String> royaltyParameters = null;
//		 if(SasUtil.isSasSupportAlipay(sas) && !isBieZhailaActivity){
//			 royaltyParameters = AlipayUtil.createPayRoyaltyParameters(sas, 
//					 totalPrice, subject, allParamMap);
//			 if(royaltyParameters != null){
//				allParamMap.put("royalty_type", royaltyParameters.key);//"10","卖家给第三方提成"
//				allParamMap.put("royalty_parameters", royaltyParameters.value);
//			 }
//		 }

		 //生成签名  	
		 allParamMap = AlipayUtil.generateAllParamsWithSignature(sas, allParamMap, AlipaySignType.MD5, true);
		 
		 //因为宝贝名字是中文，进行encode
		 allParamMap.put("subject", HtmlUtil.encodeParam(subject, Encoding.UTF8));
//		 if(royaltyParameters != null){
//		 	 allParamMap.put("royalty_parameters", HtmlUtil.encodeParam(royaltyParameters.value, Encoding.UTF8));
//		 }
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", HtmlUtil.encodeParam(transaction.getOrderGoodUrl(), Encoding.UTF8)); //链接可能带有&
		 }
		 return generateRequestQueryString(allParamMap);
	 }
	 
	 /*********************
	  * 生产wap版本的支付链接，需要sas第三方申请了手机网站支付，而且
	  * @param sas
	  * @param transaction
	  * @return
	  */
	 public static final String generateWapAliPayDirectPayURL(final Sas sas, final PortalSite portalSite, 
			 final SasOrderTransaction transaction, final long userId, final boolean isBieZhailaActivity)
	 {
		 final boolean isSasSupportAlipay = !isBieZhailaActivity && SasUtil.isSasSupportAlipay(sas);
		 final BigDecimal totalPrice = TransactionUtil.calculateTotalTransactionPrice(transaction);
		 final String supportDomain = "http://" + TransactionUtil.generateAlipaySupportDomain(sas);
		 String subject = AlipayUtil.removeInvalidURLParam(transaction.getOrderGoodName(), 85);
		 Map<String, String> allParamMap = new HashMap<String, String>();
		 allParamMap.put("service", AlipayProduct.AlipayWapTradeCreateDirect.pName);
		 allParamMap.put("partner", (isSasSupportAlipay ? sas.getAlipayPartnerId() : AlipayConstant.getPartner())); //走他们自己的支付宝
		 allParamMap.put("seller_id", (isSasSupportAlipay ? sas.getAlipayPartnerId() : AlipayConstant.getPartner())); //走他们自己的支付宝
		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
		 allParamMap.put("notify_url", supportDomain + AliPayUrl.AlipayCallbackAsynchronize.url);
		 allParamMap.put("return_url", supportDomain + AliPayUrl.AlipayCallbackSynchronize.url);
		 allParamMap.put("out_trade_no", String.valueOf(transaction.getId()));
		 allParamMap.put("payment_type", "1"); //支付类型。仅支持：1（商品购买）。
		 allParamMap.put("total_fee", totalPrice.toString());
		 allParamMap.put("subject", subject); //生成签名前需要放入,下面生成url前在encode
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", transaction.getOrderGoodUrl()); //生成签名前需要放入,下面生成url前在encode
		 }
		 allParamMap.put("it_b_pay", AlipayUtil.generateTransactionExpireTime(transaction.getExpireTime()).getKey()); //超时时间:每次动态调整
		 
		 //自定义参数
		 //allParamMap.put("extra_common_param", new OnlinePayExtraParam(userId, portalSite, !isSasSupportAlipay, !isSasSupportAlipay || isBieZhailaActivity).toString());	
		 
		 //生成签名  	
		 allParamMap = AlipayUtil.generateAllParamsWithSignature(sas, allParamMap, AlipaySignType.MD5, true);
		 
		 //因为宝贝名字是中文，进行encode
		 allParamMap.put("subject", HtmlUtil.encodeParam(subject, Encoding.UTF8));
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", HtmlUtil.encodeParam(transaction.getOrderGoodUrl(), Encoding.UTF8)); //链接可能带有&
		 }
		 return generateRequestQueryString(allParamMap);
	 }
	 
	 /**************
	  * 生成退款的链接
	  * @param sas
	  * @param transaction
	  * @return
	  */
	 public static final String generateRefundAliPayDirectPayURL(final Sas sas, final PortalSite portalSite, final SasOrderTransaction transaction, final User admin)
	 {
		 final String supportDomain = "http://" + TransactionUtil.generateAlipaySupportDomain(sas);
		 final String subject = AlipayUtil.removeInvalidURLParam(transaction.getOrderGoodName(), 85);
		 //设置参数
		 Map<String, String> allParamMap = new HashMap<String, String>();
		 allParamMap.put("service", AlipayProduct.CreateDirectPayByUser.pName);
		 allParamMap.put("partner", AlipayConstant.getPartner()); //走他们自己的支付宝
		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
		 allParamMap.put("notify_url", supportDomain + AliPayUrl.RefundAlipayCallbackAsynchronize.url);
		 allParamMap.put("return_url", supportDomain + AliPayUrl.RefundAlipayCallbackSynchronize.url);
		 allParamMap.put("out_trade_no", String.valueOf(transaction.getId()));
		 allParamMap.put("subject", subject); //生成签名前需要放入,下面生成url前在encode
		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", transaction.getOrderGoodUrl()); //生成签名前需要放入,下面生成url前在encode
		 }
		 allParamMap.put("payment_type", transaction.getThirdpartPaymentType());	
		 allParamMap.put("seller_email", SasConstant.SaihuitongAlipayAccount); //走他们自己的支付宝
		 allParamMap.put("total_fee", transaction.getChangePrice().setScale(2, BigDecimal.ROUND_HALF_UP).toString()); //退款金额
		 allParamMap.put("it_b_pay", AlipayUtil.generateTransactionExpireTime(transaction.getExpireTime()).getKey()); //每次动态调整		 
		 allParamMap.put("qr_pay_mode", QRPayMode.JumpMode.mode);
	 
		 //生成签名   
		 allParamMap = AlipayUtil.generateAllParamsWithSignature(sas, allParamMap, AlipaySignType.MD5, false);
		 
		 //因为宝贝名字是中文，进行encode
		 allParamMap.put("subject", HtmlUtil.encodeParam(subject, Encoding.UTF8));

		 if(StringUtils.isNotBlank(transaction.getOrderGoodUrl())){
			 allParamMap.put("show_url", HtmlUtil.encodeParam(transaction.getOrderGoodUrl(), Encoding.UTF8)); //链接可能带有&
		 }		 
		 return generateRequestQueryString(allParamMap);
	 }	 

	 /**************
	  * 生成直接进行支付宝退款的链接
	  * @param sas
	  * @param transaction
	  * @return
	  */
	 public static final String generateAlipayFastRefundURLByPlatformPwd(final SasRefundOrder refundOrder, final SasOrderTransaction paidTransaction)
	 {
		 //设置参数
		 Map<String, String> allParamMap = new HashMap<String, String>();
		 allParamMap.put("service", AlipayProduct.RefundFastpayByPlatforumPwd.pName);
		 allParamMap.put("partner", AlipayConstant.getPartner()); //走他们自己的支付宝
		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
		 allParamMap.put("notify_url", DomainConstant.SaiHuiTongDomain + AliPayUrl.FastRefundAlipayCallbackAsynchronize.url);
		 //allParamMap.put("return_url", DomainConstant.SaiHuiTongDomain + AliPayUrl.FastRefundAlipayCallbackAsynchronize.url);
		 allParamMap.put("seller_user_id", AlipayConstant.getPartner()); 
		 allParamMap.put("seller_email", SasConstant.SaihuitongAlipayAccount); 
		 allParamMap.put("refund_date", TimeUtil.formatCurrentTime(TimeFormat.yyyy_MM_dd_HH_mm_ss));
		 if(refundOrder.getId() < 10){
			 allParamMap.put("batch_no",TimeUtil.formatCurrentTime(TimeFormat.YYYYMMDD) + "00" + refundOrder.getId());
		 }else if(refundOrder.getId() < 100){
			 allParamMap.put("batch_no",TimeUtil.formatCurrentTime(TimeFormat.YYYYMMDD) + "0" + refundOrder.getId());
		 }else{
			 allParamMap.put("batch_no",TimeUtil.formatCurrentTime(TimeFormat.YYYYMMDD) + refundOrder.getId());
		 }
		 allParamMap.put("batch_num", "1");
		 final String detailOrderData = paidTransaction.getThirdpartTransactionId() + "^"
				 + refundOrder.getRefundPrice().setScale(2, BigDecimal.ROUND_HALF_UP) + "^"
				 + AlipayUtil.removeInvalidURLParam(refundOrder.getRefundTitle(), 85);
		 allParamMap.put("detail_data", detailOrderData);
		 //生成签名   
		 allParamMap = AlipayUtil.generateAllParamsWithSignature(null, allParamMap, AlipaySignType.MD5, false);
		 
		 //因为宝贝名字是中文，进行encode
		 allParamMap.put("detail_data", HtmlUtil.encodeParam(detailOrderData, Encoding.UTF8)); 

		 return AliPayUrl.HttpsPayUrl.url + generateRequestQueryString(allParamMap);
	 }	
//	 
//	 /**************
//	  * 生成针对即时到账交易关闭的url
//	  * @param transaction
//	  * @return
//	  */
//	 private static final String generateTransactionCloseRequestURL(final Sas sas, 
//			 final SasOrderTransaction transaction, final boolean isBieZhailaActivity)
//	 {
//		 if(!isBieZhailaActivity && SasUtil.isSasSupportAlipay(sas)){
//			 return null;
//		 }
//		 Map<String, String> allParamMap = new HashMap<String, String>();
//		 allParamMap.put("service", AlipayProduct.CloseOrderDirectPayByUser.pName);
//		 allParamMap.put("partner", AlipayConstant.getPartner()); //走他们自己的支付宝
//		 allParamMap.put("_input_charset", AlipayConstant.InputCharset.type.toLowerCase());
//		 allParamMap.put("out_trade_no", String.valueOf(transaction.getId()));
//		 allParamMap.put("trade_role", "S");
//		 allParamMap = AlipayUtil.generateAllParamsWithSignature(sas, allParamMap, AlipaySignType.MD5, true);
//		 return generateRequestQueryString(allParamMap);
//	 }
//	 
//	 /*************
//	  * 返回
//	  * <?xml version="1.0" encoding="utf-8"?><alipay><is_success>T</is_success></alipay>
//	  * 或者
//	  * <?xml version="1.0" encoding="utf-8"?><alipay><is_success>F</is_success><error>TRADE_STATUS_NOT_AVAILD</error></alipay>
//	  * @param sas
//	  * @param transaction
//	  * @param isBieZhailaActivity
//	  * @return
//	  */
//	 public static final boolean doCloseTransaction(final Sas sas, 
//			 final SasOrderTransaction transaction, final boolean isBieZhailaActivity, final int tryTimes)
//	 {
//		 final String text = IOUtil.readTextFromHttpURL(AliPayUrl.HttpsPayUrl.url 
//				 + AlipayUtil.generateTransactionCloseRequestURL(sas, transaction, isBieZhailaActivity), tryTimes);
//		 if(text == null || text.length() < 1){
//			 return false;
//		 }
//		 if(text.toLowerCase().contains("error")){
//			 logger.error("Fail to close alipay transaction order, response=" + text + ", transaction=" + transaction.getId());
//			 return false;
//		 }
//		 System.out.println(text);
//		 return true;
//	 }
	 
	 /**
	 * 描述： 通过分润金额100%的方式， 将金额全额转账给第三方支付宝账号
	 * <p>
	 * 说明：
	 * 一、支付宝费率收取规则： 目前接口收费 
	 * （1）阶梯费率产品：成功一笔扣一笔，每笔1.2%，随着营业额的累计，交易额累计到达一个阶段会按照一个阶段的费率进行单笔扣费。 
	 * （2）预付费产品，是需要就直接支付一年的交易服务费， 可以根据驴管家的交易量选择， 600元包6万，1800元包20万，3600元包45万，超出包量的每笔1.2%收取，一年
	 *     合同期限，预付了的款项在套餐流量使用范围内是不需要再支付其他的费用的
	 * 二、分润金额规则 
	 *  （1）分润的总金额 <= 付款总金额 - 支付宝手续费
	 *  （2）分润金额不能低于0.01元
	 * </p>
	 * @param totalPrice  交易总金额
	 * @param refundReceiverAccount:如果是退款， 这个参数不为空
	 * @param rate  赛会通向各个俱乐部收取的佣金比例
	 * @param aliPayDirectPayParams 请求参数对象
	 */
//	private static final BinaryEntry<String, String> createPayRoyaltyParameters(final Sas sas, final BigDecimal totalPrice, 
//			String royaltyDesc, final Map<String, String> allParamMap) 
//	{		
//		if(StringUtils.isBlank(sas.getAlipay()) || SasConstant.SaihuitongAlipayAccount.equals(sas.getAlipay())){
//			return null; 
//		}
//		//royaltyDesc = HtmlUtil.encodeParam(royaltyDesc, Encoding.UTF8);
//		if(StringUtils.isBlank(royaltyDesc)){
//			royaltyDesc = "赛会通交易";
//		}else{
//			royaltyDesc = HtmlUtil.subStringWhenAccessLimit(royaltyDesc.replaceAll(HtmlUtil.WhiteSpaceReg, ""), 10);
//		}
//		//开始分润
//		final SasCommissionType commissionType = SasCommissionType.parse(sas.getCommissionType());
//		if(SasUtil.isSasSupportAlipay(sas))
//		{	
//			//用第三方自己的支付
//			if(commissionType == SasCommissionType.Zero || commissionType.alipayRatio < 0.007){
//				return null;
//			}
//			//除了1.2%支付宝佣金，我们需要把100%的收入转给第三方
//			final BigDecimal profit = totalPrice.multiply(new BigDecimal(commissionType.alipayRatio - 0.006)); 
//			final String royalty_parameters = SasConstant.SaihuitongAlipayAccount + "^" + profit.setScale(2, BigDecimal.ROUND_HALF_DOWN) +"^赛会通分润";
//			return new BinaryEntry<String, String>("10", royalty_parameters);			
//		} 
//		else {//用我们的分润的模式
//			//除了1.2%支付宝佣金，我们需要把100%的收入转给第三方
//			final BigDecimal profit = (commissionType == null || commissionType.alipayRatio < 0.006) ? totalPrice.multiply(new BigDecimal(0.994)) :
//				totalPrice.multiply(new BigDecimal(1 - commissionType.alipayRatio)); 
//			final String royalty_parameters = sas.getAlipay() + "^" + profit.setScale(2, BigDecimal.ROUND_DOWN) +"^" + royaltyDesc;
//			return new BinaryEntry<String, String>("10", royalty_parameters);
//		}
//		//allParamMap.put("royalty_type", "10");//"10","卖家给第三方提成"
//		//allParamMap.put("royalty_parameters", royalty_parameters);
//	}
	
	/***************
	 * 退款走我们的流水
	 * @param refundReceiverAccount
	 * @param totalPrice
	 * @param royaltyDesc
	 * @param allParamMap
	 * @return
	 */
	private static final BinaryEntry<String, String> createRefundRoyaltyParameters(final String refundReceiverAccount, final BigDecimal totalFeeWithCommission, 
			String royaltyDesc, final Map<String, String> allParamMap) 
	{		
		if(StringUtils.isBlank(refundReceiverAccount)){
			return null; 
		}
		//royaltyDesc = HtmlUtil.encodeParam(royaltyDesc, Encoding.UTF8);
		if(StringUtils.isBlank(royaltyDesc)){
			royaltyDesc = "赛会通退款";
		}else {
			royaltyDesc = HtmlUtil.subStringWhenAccessLimit(royaltyDesc.replaceAll(HtmlUtil.WhiteSpaceReg, ""), 40);
		}
		//开始分润
		//除了1.2%支付宝佣金，我们需要把100%的收入转给第三方
		final String royalty_parameters = refundReceiverAccount + "^" 
				+ totalFeeWithCommission.multiply(BigDecimal.valueOf(0.994)).setScale(2, BigDecimal.ROUND_HALF_UP) +"^" + royaltyDesc;
		return new BinaryEntry<String, String>("10", royalty_parameters);		
		//allParamMap.put("royalty_type", "10");//"10","卖家给第三方提成"
		//allParamMap.put("royalty_parameters", royalty_parameters);
	}
//	// 判断分润金额的有效性
//	private boolean profitLegal(BigDecimal profit, BigDecimal totalPrice) {
//		boolean isMin = profit.compareTo(BigDecimal.valueOf(0.01)) > 0;
//		boolean isMax = profit.compareTo(totalPrice.multiply(BigDecimal
//				.valueOf(1 - 0.012))) < 0;
//		return isMin && isMax;
//	}
	 
    /** 
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static final Map<String, String> filterNonAndSignParams(final Map<String, String> sArray)
    {
        final Map<String, String> result = new HashMap<String, String>();
        if (sArray == null || sArray.size() <= 0) {
            return result;
        }
        for (final Map.Entry<String, String> entry : sArray.entrySet()) {
            final String value = entry.getValue();
            if (StringUtils.isBlank(value)|| entry.getKey().equalsIgnoreCase("sign")
                || entry.getKey().equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static final String generateRequestQueryString(final Map<String, String> params) {
        final List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        final StringBuilder paramString = new StringBuilder("");
        for (final String key : keys) {
        	if(paramString.length() > 0){
        		paramString.append("&" + key + "=" + params.get(key));
        	}else{
        		paramString.append(key + "=" + params.get(key));
        	}
        }
        return paramString.toString();
    }
    
    /**
     * 生成签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
    public static final String generateRequestSignature(final Sas sas, final Map<String, String> sPara, AlipaySignType signType, 
    		final boolean canUseSasAlipayKeyToGenSign)
    {
    	final String prestr = AlipayUtil.generateRequestQueryString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        if(sas != null && canUseSasAlipayKeyToGenSign && SasUtil.isSasSupportAlipay(sas)){//采用赛事自己的支付
        	if(signType == null || signType == AlipaySignType.MD5 ) {
            	return AlipayMD5.sign(prestr, sas.getAlipayMD5SignKey(), Encoding.UTF8);
            }else if(signType == AlipaySignType.RSA ){
            	TransactionUtil.logActivityOrderMessage("SignError", 0, IdUtil.convertTolong(sPara.get("out_trade_no"), 0), "RAS Not Support of sas "+sas.getId());
            	throw new ServerUnknownException("Fatal Error: RAS Not Support of sas "+sas.getId());
            }
        }else{//走赛会通模式的支付
	    	if(signType == null || signType == AlipaySignType.MD5 ) {
	        	return AlipayMD5.sign(prestr, AlipayConstant.getMD5PrivateKey(), Encoding.UTF8);
	        }else if(signType == AlipaySignType.RSA ){
	        	return AlipayRSA.sign(prestr, AlipayConstant.RSAPrivateKey, Encoding.UTF8);
	        }
        }
        return "";
    }
	
    /**
     * 生成要请求给支付宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    public static Map<String, String> generateAllParamsWithSignature(final Sas sas, final Map<String, String> sParaTemp, 
    		final AlipaySignType signType,  final boolean canUseSasAlipayKeyToGenSign) {
        //除去数组中的空值和签名参数
        final Map<String, String> sPara = AlipayUtil.filterNonAndSignParams(sParaTemp);
        //生成签名结果
        final String mysign = AlipayUtil.generateRequestSignature(sas, sPara, signType, canUseSasAlipayKeyToGenSign);

        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", signType.type);

        return sPara;
    }
    
    /**
     * MAP类型数组转换成NameValuePair类型
     * @param properties  MAP类型数组
     * @return NameValuePair类型数组
     */
    public final static NameValuePair[] generatNameValuePair(final Map<String, String> properties) {
        final NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
        int i = 0;
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            nameValuePair[i++] = new NameValuePair(entry.getKey(), entry.getValue());
        }
        return nameValuePair;
    }

    /**************
     * 验证消息是否是支付宝发出的合法消息
     * @param params 通知返回来的参数数组
     * @param alipayKey
     * @param needVerifyNotifyId: 是否需要触发远程调用验证notifyid
     * @return
     */
    public static final boolean verifyRequestSignatureAndNotifyId(final Sas sas, final Map<String, String> params, 
    		final AlipayKey alipayKey, final boolean isUsingSaihuitongAlipayAccount) {
    	final String notifyId = params.get("notify_id");
    	if(StringUtils.isBlank(notifyId)){
 	    	logger.fatal("Error notifyId of alipay callback request: " + ReflectionToStringBuilder.toString(params));
    		return false;
    	}
    	//验证签名: isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
    	final AlipaySignType signType = AlipaySignType.parse(params.get("sign_type"));
    	final String sign = params.get("sign");
 	    if(StringUtils.isBlank(sign) || !AlipayUtil.verifySignature(sas, params, sign, alipayKey, signType, isUsingSaihuitongAlipayAccount)){
 	    	logger.fatal("Error sign of alipay callback request: " + ReflectionToStringBuilder.toString(params));
 	    	return false;
 	    }
    	return true;
    }

    /*************
     *  根据反馈回来的信息，生成签名结果
     * @param Params 通知返回来的参数数组
     * @param sign 比对的签名结果
     * @param alipayKey 阿里的公钥， ras会用到， md5用到的是我们的私钥
     * @return 生成的签名结果
     */
	public static boolean verifySignature(final Sas sas, final Map<String, String> params, final String sign, 
			final AlipayKey alipayKey, final AlipaySignType signType, final boolean isUsingSaihuitongAlipayAccount)
	{		
    	//过滤空值、sign与sign_type参数
    	final Map<String, String> sParaNew = AlipayUtil.filterNonAndSignParams(params);
    	//获取待签名字符串
        final String signContent = AlipayUtil.generateRequestQueryString(sParaNew);
        //获得签名验证结果
        if(sas != null && !isUsingSaihuitongAlipayAccount) //SasUtil.isSasSupportAlipay(sas))
        {
        	if(signType == null || signType == AlipaySignType.MD5) {
	        	return AlipayMD5.verify(signContent, sign, sas.getAlipayMD5SignKey(), AlipayConstant.InputCharset);
	        }else if(signType == AlipaySignType.RSA ){
	        	TransactionUtil.logActivityOrderMessage("VerifySignError", 0, IdUtil.convertTolong(params.get("out_trade_no"), 0),
	        			"RAS Not Support of sas "+sas.getId());
            	throw new ServerUnknownException("Fatal Error: Verify RAS Sign Not Support of sas "+sas.getId());
	        }
        }else{
        	if(signType == null || signType == AlipaySignType.MD5) {
	        	return AlipayMD5.verify(signContent, sign, AlipayConstant.getMD5PrivateKey(), AlipayConstant.InputCharset);
	        }else if(signType == AlipaySignType.RSA ){
	        	return AlipayRSA.verify(signContent, sign, alipayKey.publicKey, AlipayConstant.InputCharset);
	        }
        }
        return false;
    }

    /**
    * 获取远程服务器ATN结果
    * @param urlvalue 指定URL路径地址
    * @return 服务器ATN结果
    * 验证结果集：
    * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
    * true 返回正确信息
    * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
    */
    private static String readHTTPURLResponse(final String urlvalue) {
        try {
            final URL url = new URL(urlvalue);
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
                .getInputStream()));
            return in.readLine().toString();
        } catch (Exception e) {
            logger.error("Fail to readHTTPURLResponse:"+e.getMessage(), e);
            return "";
        }
    }
    
	 /*********************
	  * 读取request中的参数列表
	  * @param request
	  * @param paramNames
	  * @return
	  */
	 public static final Map<String, String> readPostedAllParameters(final HttpServletRequest request)
	{
		 //获取支付宝POST过来反馈信息
		 final Map<String,String> params = new HashMap<String,String>();
		 final Map<String,String[]> requestParams = request.getParameterMap();
		 for (Entry<String,String[]> entry: requestParams.entrySet()) 
		 {
			 final String name = entry.getKey();
			 final String[] values = entry.getValue();
			 final StringBuilder valueStr = new StringBuilder("");
			 for (int i = 0; i < values.length; i++) {
				 if(i == values.length - 1){
					 valueStr.append(values[i]);
				 }else{
					valueStr.append(values[i] + ",");
				 }
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr.toString());
		 }
		 return params;
	}
	 
	public static void main(String[] args)
	{
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.create.direct.pay.by.user");
        sParaTemp.put("partner", AlipayConstant.getPartner());
        sParaTemp.put("seller_id", "zhuliming_msn@hotmail.com");
        sParaTemp.put("_input_charset", Encoding.UTF8.type);
		sParaTemp.put("payment_type", "1");
		sParaTemp.put("notify_url", "http://www.saihuitong.com/alipay");
		sParaTemp.put("return_url", "http://www.saihuitong.com/alipay");
		sParaTemp.put("out_trade_no", "S001");
		sParaTemp.put("subject", "goodname1");
		sParaTemp.put("total_fee", "12");
		sParaTemp.put("show_url", "http://www.saihuitong.com/3");
		sParaTemp.put("body", "good desc");
		sParaTemp.put("it_b_pay", "1d");
	}
}
