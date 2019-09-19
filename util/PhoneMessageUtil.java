/**
 * 
 */
package com.sas.core.util;

/**
 * 短信相关的util
 * @author zhuliming
 *
 */
public class PhoneMessageUtil {
	
	
	/**************
	 * 要移除特殊的词汇， 这些词不允许出现在短信内容里面
	 * 同时保证3-8个字符
	 * @param clubName
	 * @return
	 */
	public static final String removeIllegalWordsFromName(String name)
	{
		if(name == null){
			return "";
		}
		return name.replaceAll("私人订制", "");
	}
}
