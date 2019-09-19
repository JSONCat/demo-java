/**
 * 
 */
package com.sas.core.util;


/**
 * java端返回的中英文消息
 * @author Administrator
 *
 */
public class LanguageUtil {
	
	
//	public static final String getMessage
//
//	/**********
//	 * 所有的消息文本
//	 * Key 存储中文， value存储英文
//	 */
//	private static final Map<String, BinaryEntry<String, String>> messageMap = new HashMap<String, BinaryEntry<String, String>>();
//	
//	/*************
//	 * 返回中英文消息
//	 * @param key
//	 * @param language
//	 * @return
//	 */
//	public static final String message(final Class cls, final String msgKey, final SasLanguage language){
//		return LanguageUtil.message(cls, msgKey, language == SasLanguage.ChineseOnly);
//	}
//	
//	public static final String message(final Class cls, final String msgKey, final char language){
//		return LanguageUtil.message(cls, msgKey, language == SasLanguage.ChineseOnly.type);
//	}
//	
//	public static final String message(final Class cls, final String msgKey, final Sas sas){
//		return LanguageUtil.message(cls, msgKey, sas.getLanguageSupport() == SasLanguage.ChineseOnly.type);
//	}
//	
//	public static final String message(final Class cls, final String msgKey, final boolean isChinese){
//		final String key = LanguageUtil.createMessageKey(cls, msgKey);
//		final BinaryEntry<String, String> msg = messageMap.get(key);
//		return msg == null ? "" : (isChinese ? msg.key : msg.value);
//	}
//	
//	private static final String createMessageKey(final Class cls, final String msgKey)
//	{
//		if(msgKey == null){
//			return cls.getName(); 
//		}else{
//			return cls.getName() + "-" + msgKey; 
//		}
//	}
//	
//	/*************
//	 * 添加消息文本
//	 * @param cls
//	 * @param method
//	 * @param msgKey
//	 * @param chineseText
//	 * @param englishText
//	 */
//	public static final void addMessageText(final Class cls, final String msgKey, 
//			final String chineseText, final String englishText)
//	{
//		messageMap.put(LanguageUtil.createMessageKey(cls, msgKey), new BinaryEntry<String, String>(chineseText, englishText));
//	}
}
