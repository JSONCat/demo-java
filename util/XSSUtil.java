/**
 * @Title: XSSUtil.java
 * @Package com.sas.core.util
 * @author yunshang_734@163.com
 * @date Dec 24, 2014 10:16:26 AM
 * @version V1.0
 */
package com.sas.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;

/**
 * @ClassName: XSSUtil
 * @Description: 萌萌哒健哥写的防XSS攻击的方法
 * @author yunshang_734@163.com
 * @date Dec 24, 2014 10:16:26 AM
 */
public class XSSUtil {
	// 特殊字符
	private static String specialCharacters = "[`~!@#$%^&*()+=|{}':;\"',\\[\\].<>/?~！@#￥%……&*（）——+|｛｝【】‘；：”“’。，、？]";

	private static Pattern scriptPattern = Pattern.compile("</?script[^>]*>");

	private static Pattern buttonPattern = Pattern.compile("</?button[^>]*>");

	private static Pattern inputPattern = Pattern.compile("</?input[^>]*>");

	private static Pattern onEventPattern = Pattern
			.compile("on(?:error|key\\w+|mouse\\w+|click|touch)=['\"].*['\"]");

	/**
	 * 把尖括号转义成实体字符
	 * 
	 * @param text
	 * @return
	 */
	public static String encode(String text) {
		if (text == null)
			return null;
		return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	public static String[] filter(final String[] texts, final boolean needRemoveXMLLetters) {
		if(ArrayUtils.isEmpty(texts)){
			return texts;
		}
		for(int i=0; i<texts.length;i++){
			texts[i] = XSSUtil.filter(texts[i], needRemoveXMLLetters);
		}
		return texts;
	}
	/**
	 * 过滤<script></script>和onxxx事件脚本
	 * 
	 * @param text
	 * @param needXMLLetters:是否需要过滤&#21;以及<?xml:namespace=\"0\">
	 * @return
	 */
	public static String filter(String text, final boolean needRemoveXMLLetters) {
		if (text == null) {
			return "";
		}
		if(needRemoveXMLLetters){
			text = text.replaceAll("(&#[0123456789]{1,3};)|(<\\?xml:namespace[^>]{1,50}>)|(position *: *fixed *;*)|(POSITION *: *FIXED *;*)", "");
		}
		text = HtmlUtil.unescape(text, true); //对&amp;这些进行转义
		while (true) {
			Matcher matcher = scriptPattern.matcher(text);
			if (matcher.find()) {
				text = matcher.replaceAll("");
			} else {
				break;
			}
		}
		while (true) {
			Matcher matcher = onEventPattern.matcher(text);
			if (matcher.find()) {
				text = matcher.replaceAll("");
			} else {
				break;
			}
		}
		return text;
	}

	public static String removeButtomAndInput(String content) {
		if (content == null) {
			return content;
		}
		while (true) {
			Matcher matcher = inputPattern.matcher(content);
			if (matcher.find()) {
				content = matcher.replaceAll("");
			} else {
				break;
			}
		}
		while (true) {
			Matcher matcher = buttonPattern.matcher(content);
			if (matcher.find()) {
				content = matcher.replaceAll("");
			} else {
				break;
			}
		}
		return content;
	}

	/**
	 * 过滤所有特殊字符
	 * 
	 * @param sourceStr
	 * @return
	 */
	public static String clearAllSpecialCharacters(String sourceStr) {
		if (sourceStr == null) {
			return null;
		}
		Pattern p = Pattern.compile(specialCharacters);
		Matcher m = p.matcher(sourceStr);
		return m.replaceAll("").trim();
	}

	public static void main(String[] args) {
		String text = "<script><script><scr<script>ipt>alert(\"test\")</script></script></script>";
		System.out.print(XSSUtil.filter(text, true));

		String text2 = "<div onclick='alert(1)' onkeyup='alert(2)' onmousedown='alert(3)'></div>";
		System.out.print(XSSUtil.filter(text2, true));
		
		System.out.println(System.currentTimeMillis());
	}
}