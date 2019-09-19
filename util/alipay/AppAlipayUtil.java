/**
 * 
 */
package com.sas.core.util.alipay;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.sas.core.constant.AlipayConstant;
import com.sas.core.constant.AppAlipayConstant;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasOrderTransaction;
import com.sas.core.util.TransactionUtil;
import com.sas.core.util.meta.SasUtil;

/**
 * APP支付相关
 * @author Administrator
 *
 */
public class AppAlipayUtil {
	
	public static final Logger logger = Logger.getLogger(AppAlipayUtil.class);
	
	private static AlipayClient alipayClient = null;
	
	/**************
	 * AlipayClient的实现类都是线程安全的，所以没有必要每次API请求都新建一个AlipayClient实现类；
	 * 创建AlipayClient实现类的实例时，指定format=json，相比xml格式，可以减少数据传输量，提升API请求效率。
	 * @return
	 */
	private static final AlipayClient getAlipayClient()
	{
		if(alipayClient == null)
		{
			synchronized(AppAlipayUtil.class)
			{
				if(alipayClient == null)
				{
					alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", 
							AppAlipayConstant.getAPPId(), AppAlipayConstant.getAPP_RSA_SHA1_PRIVATE(), "json", 
							AlipayConstant.InputCharset.type, AppAlipayConstant.getAPP_RSA_SHA1_PUBLIC());
				}
				
			}
		}
		return alipayClient;
	}
	
	/************
	 * 发送支付请求
	 * @param sas
	 * @param transaction
	 * @return
	 */
	public static final String sendPayRequest(final Sas sas, final SasOrderTransaction transaction)
	{
		if(SasUtil.isSasSupportAlipay(sas)){
			return null;
		}
		final BigDecimal totalPrice = TransactionUtil.calculateTotalTransactionPrice(transaction);
		//实例化客户端
		//实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		final AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		//SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setBody(transaction.getOrderGoodName());
		model.setSubject(transaction.getOrderGoodName());
		model.setOutTradeNo(String.valueOf(transaction.getId()));
		model.setTimeoutExpress("30m");
		model.setTotalAmount(totalPrice.toString());
		model.setProductCode("QUICK_MSECURITY_PAY");
		request.setBizModel(model);
		request.setNotifyUrl(AppAlipayConstant.CallBackURL);
		try {
			//这里和普通的接口调用不同，使用的是sdkExecute
		    AlipayTradeAppPayResponse response = AppAlipayUtil.getAlipayClient().sdkExecute(request);
		    return response.getBody();//就是orderString 可以直接给客户端请求，无需再做处理。
		} catch (Exception e) {
		    logger.error("fail to sendPayRequest : " + e.getMessage(), e);
		    return null;
		}
	}
	
	/***********
	 * 验证支付回调的请求是否正确
	 * @param request
	 * @return
	 */
	public static final boolean verifyCallbackRequest(final Map<String,String> params)
	{
		//切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
		//boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
		try {
			System.out.println("AppAlipayConstant:" + AppAlipayConstant.getAPP_RSA_SHA1_PUBLIC());
			return AlipaySignature.rsaCheckV1(params, AppAlipayConstant.getAPP_RSA_SHA1_PUBLIC(), 
					AlipayConstant.InputCharset.type);
		} catch (Exception e) {
			 logger.error("fail to verifyCallbackRequest : " + e.getMessage(), e);
			 return false;
		}
	}
}
