/**
 * 
 */
package com.sas.core.util.meta;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.util.ValidatorUtil;

/**
 * 邮件util
 * @author zhuliming
 *
 */
public class EmailUtil {

	
	/************
	 * 将邮件列表转换成列表
	 * @param sb
	 * @return
	 */
	public static final List<String> processEmails2RightFormat(final String emailsString, final DividerChar splitChars){
		if(StringUtils.isBlank(emailsString)){
			return new ArrayList<String>(0);
		}
		final String[] emailArray = emailsString.split(splitChars.chars);
		final List<String> list = new ArrayList<String>(emailArray.length);
		for(final String email: emailArray){
			final String rightEmail = EmailUtil.processOneEmail2RightFormat(email);
			if(rightEmail != null){
				list.add(rightEmail);
			}
		}		
		return list;
	}
	
	public static final String[] processEmails2RightFormatAsArray(final String emailsString, final DividerChar splitChars){
		final List<String> list = EmailUtil.processEmails2RightFormat(emailsString, splitChars);
		return list.toArray(new String[list.size()]);
	}
	
	/*****************
	 * 对邮箱中的特殊字符做特殊处理
	 * @param email
	 * @return
	 */
	public static final String processOneEmail2RightFormat(String email)
	{ 
		if(StringUtils.isBlank(email)){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");		
		for (int i = 0; i < email.length(); i++) { 
			char c = email.charAt(i);
			if(c == ' ' || c == '　' || c == '\n' || c == '\t' || c == '\r'){
				continue;
			}
			final String destChar = ValidatorUtil.digitAndLetterChineseMap.get(String.valueOf(c));
			if(destChar != null){
				sb.append(destChar);
			}else{
				sb.append(c);
			}
		}
		email = sb.toString().toLowerCase();
		if(! ValidatorUtil.emailValidate(email)){
			return "";
		}
		return email;
	}
}
