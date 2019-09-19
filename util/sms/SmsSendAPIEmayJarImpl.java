/**
 * 
 */
package com.sas.core.util.sms;

import org.apache.log4j.Logger;

import cn.emay.sdk.client.api.Client;

import com.sas.core.constant.PhoneMessageConstant.PhoneMsgPriority;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.util.ThreadUtil;

/**
 * 亿美软通：导入JAR的方式协议
 * @author Administrator
 *
 */
public class SmsSendAPIEmayJarImpl implements SmsSendAPI {

	private static final Logger logger = Logger.getLogger("com.logger.sms");
	
	private final String smsSerialNo;
	private final String smsKey;
	private final String url;
	private Client smsClient = null;
	
	public SmsSendAPIEmayJarImpl(String smsSerialNo, String smsKey, String url) {
		super();
		this.smsSerialNo = smsSerialNo;
		this.smsKey = smsKey;
		this.url = url;
	}

	@Override
	public void init() 
	{
		new Thread(){
			public void run()
			{
				int tryTimes = 0;
				while(smsClient == null && tryTimes++ < 60)
				{
					try{
						smsClient = new Client(smsSerialNo, smsKey);
						//smsClient.registEx(smsKey);
						return;
					}catch(Exception ex){
						logger.fatal("Fail to create smsClient, smsSerialNo=" + smsSerialNo 
								+ ",smsKey=" + smsKey + ", try times=" + tryTimes, ex);
						ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds * tryTimes);
					}
				}
			}
		}.start();	
	}
	
	@Override
	public boolean sendSms(String mobile, String content) {
		try{
			final int status = smsClient.sendSMS(new String[]{mobile}, content, PhoneMsgPriority.Important.priority);
			if(status == 0){
				logger.error("SMS Succ: " + mobile +" | " + content);
				return true;
			}else{
				logger.fatal("SMS Failed: " + mobile +" | " + content);
				return false;
			}
		}catch(Exception ex){
			logger.fatal("SMS Failed: " + mobile +" | " + content, ex);	
			return false;
		}	
	}

	@Override
	public int querySmsBalance() {
		try{
			return (int)(smsClient.getBalance() * 10);
		}catch(Exception ex){
			logger.fatal("SMS querySmsBalance: " + ex.getMessage(), ex);	
			return -1;
		}	
	}

}
