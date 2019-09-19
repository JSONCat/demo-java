/**
 * 
 */
package com.sas.core.util.sms;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.util.IOUtil;
import com.sas.core.util.IdUtil;

/**
 * 杭州三体科技短信：HTTP协议
 * @author Administrator
 *
 */
public class SmsSendAPISanTiImpl implements SmsSendAPI {

	private static final Logger logger = Logger.getLogger("com.logger.sms");
	
    private final static String[] strDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	private static final String SmsURL = "http://sms4st.cmfree.cn/sms.php";
	
	private static final String SmsBalanceURL = "http://sms4st.cmfree.cn/sms.php";
	
	private final String appId;
	private final String appKey;
	private final String modelId;
	
	public SmsSendAPISanTiImpl(String appId, String appKey, String modelId) {
		super();
		this.appId = appId;
		this.appKey = appKey;
		this.modelId = modelId;
	}

	@Override
	public void init() {
	}


	@Override
	public boolean sendSms(String phone, String content) {
		try{
			final String sign = EncoderByMd5(appKey+appId+phone).toLowerCase();
			final String postData = "type=pt&app_id="+appId+"&mode_id="+modelId+"&sign="
					+sign+"&vars="+content.replaceAll("\\|", "")+"&to_phone="+phone;
			final URL myurl = new URL(SmsURL);
			final HttpURLConnection urlc = (HttpURLConnection) myurl.openConnection();
			urlc.setConnectTimeout(5000);
			urlc.setReadTimeout(5000);
			urlc.setDoOutput(true);   
			urlc.setDoInput(true);
			final DataOutputStream server = new DataOutputStream(urlc.getOutputStream());
			server.write(postData.getBytes("utf-8"));
			server.close();
			final String result = IOUtil.readTextFromHttpURL(urlc.getInputStream(), Encoding.UTF8);
			if(result == null || result.length() < 1){
				return false;
			}
			final String[] array = result.split(",");
			return "0".equals(array[0]);
		}catch(Exception ex){
			logger.error("Fail to send sms: phone=" + phone + ", sms=" + content, ex);
			return false;
		}
	}

	@Override
	public int querySmsBalance() {
		try{
			final String sign = EncoderByMd5(appKey+appId).toLowerCase();	
			final String postData = "type=bl&app_id="+appId+"&sign="+sign;	
			final URL myurl = new URL(SmsBalanceURL);
			final HttpURLConnection urlc = (HttpURLConnection) myurl.openConnection();
			urlc.setConnectTimeout(5000);
			urlc.setReadTimeout(5000);
			urlc.setDoOutput(true);   
			urlc.setDoInput(true);
			final DataOutputStream server = new DataOutputStream(urlc.getOutputStream());
			server.write(postData.getBytes("utf-8"));
			server.close();	
			final String result = IOUtil.readTextFromHttpURL(urlc.getInputStream(), Encoding.UTF8);
			if(result == null || result.length() < 1){
				return 0;
			}
			final String[] array = result.split(",");
			if(array.length < 2){
				return 0;
			}
			return IdUtil.convertToInteger(array[1], 0);
		}catch(Exception ex){
			logger.error(ex);
			return 0;
		}
	}


	
	public static String EncoderByMd5(String str) throws Exception{
        final MessageDigest md5= MessageDigest.getInstance("MD5");
        final byte[] bByte = md5.digest(str.getBytes("utf-8"));
        final StringBuilder sBuffer = new StringBuilder();
        for (int i = 0; i < bByte.length; i++) {
            sBuffer.append(byteToArrayString(bByte[i]));
        }
        return sBuffer.toString();
    }
	
    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (iRet < 0) {
        	iRet += 256;
        }
        final int iD1 = iRet / 16;
        final int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }
    
    public static final void main(String[] agrs){
//        SmsSendAPI marketingPromotionSmsAPI = new SmsSendAPISanTiImpl("10131", "a64860b233", "111282");
//        marketingPromotionSmsAPI.sendSms("13758147354", "【中山市登山运动协会】您好 朱立明，感谢您报名开年年会活动，请于2-10准时报到|带上身份证，退订TD");
        
        SmsSendAPI notificationSmsAPI = new SmsSendAPISanTiImpl("10130", "6eaf8cadf5", "111281");
        notificationSmsAPI.sendSms("13758147354",  "【中山市登山运动协会】您好 朱立明，您已成功报名开年年会活动，请于2-10准时报到|带上身份证，退订TD".replaceAll("\\|", ""));
    }

}
