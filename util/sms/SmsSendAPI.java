/**
 * 
 */
package com.sas.core.util.sms;

/**
 * 短信发送器
 * @author Administrator
 *
 */
public interface SmsSendAPI {

	/*******
	 * 初始化
	 */
	public void init();
	
	/*********
	 * 发送短信， 成功返回true， 该API不做关键字检测
	 * @param mobile
	 * @param content
	 * @return
	 */
	public boolean sendSms(final String mobile, final String content);
	
	/*******
	 * 查询短信余额条数
	 * @return
	 */
	public int querySmsBalance();
}
