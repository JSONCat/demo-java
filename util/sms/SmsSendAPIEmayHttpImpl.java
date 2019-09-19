/**
 * 
 */
package com.sas.core.util.sms;

import org.apache.log4j.Logger;

import com.sas.core.constant.PhoneMessageConstant.PhoneMsgPriority;
import com.sas.core.util.IdUtil;
import com.sas.core.util.RequestUtil;
import com.sas.core.util.emay.SDKHttpClient;

/**
 * 亿美软通：http协议
 * @author Administrator
 *
 */
public class SmsSendAPIEmayHttpImpl implements SmsSendAPI {
	 
	private static final Logger logger = Logger.getLogger("com.logger.sms");
	
    private final String sn;// 软件序列号,请通过亿美销售人员获取
    private final String key;// 序列号首次激活时自己设定
    private final String password;// 密码,请通过亿美销售人员获取
    private final String baseUrl; //http请求的基本的url前缀
    
	public SmsSendAPIEmayHttpImpl(String sn, String key, String password, String baseUrl) {
		super();
		this.sn = sn;
		this.key = key;
		this.password = password;
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
	}

	@Override
	public void init() {
	}

	@Override
	public boolean sendSms(String phone, String message) {
		try{
			message = RequestUtil.encodeURLParam(message, message);
		    final String param = "cdkey=" + sn + "&password=" + key + "&phone=" + phone + "&message="
		    		+ message + "&seqid=" + System.currentTimeMillis()
		    		+ "&smspriority=" + PhoneMsgPriority.Important.priority;
			final String url = baseUrl + "sendsms.action";
		    final String ret = SDKHttpClient.sendSMS(url, param);
		    return ret != null && "0".equals(ret.trim());
		}catch(Exception ex){
			logger.error("fail send sms : phone=" + phone + ", message=" + message, ex);
			return false;
		}
	}


	@Override
	public int querySmsBalance() {
		final String param = "cdkey=" + sn + "&password=" + key;
	    final String url = baseUrl + "querybalance.action";
	    return (int)(IdUtil.convertToDouble(SDKHttpClient.getBalance(url, param), 0) * 10);
	}

}
