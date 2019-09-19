/**
 * 
 */
package com.sas.core.util;

import com.sas.core.constant.CommonConstant.EnvironmentType;
import com.sas.core.constant.TimeConstant.Miliseconds;

/**
 * 惠泽保险
 * @author Administrator
 *
 */
public class InsurranceUtil {

	/********
	 * 一些常量
	 */
	public static String partnerId = "";
	
	public static String key = "";
	
	/****************
	 * 获取远程URL
	 * @param type
	 * @return
	 */
	private static final String getRemoteURL(final EnvironmentType type){
		if(type == EnvironmentType.Online){
			return "";
		}
		return "http://testchannel.hzins.com/api/";
	}


	public static String getKey() {
		while(key == null || key.length() < 1){
			TransactionUtil.logOrderMessage("No insurrance key", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return key;
	}
	public static String getPartnerId() {
		while(partnerId == null || partnerId.length() < 1){
			TransactionUtil.logOrderMessage("No insurrance partnerId", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return partnerId;
	}

	public static void setPartnerId(String partnerId) {
		InsurranceUtil.partnerId = partnerId;
	}

	public static void setKey(String key) {
		InsurranceUtil.key = key;
	}
	
	
}
