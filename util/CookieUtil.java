package com.sas.core.util;

import com.sas.core.constant.CommonConstant.CookieField;
import com.sas.core.constant.TimeConstant.Seconds;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author zhulm
 *
 */
public final class CookieUtil {

	private static final Logger logger = Logger.getLogger(CookieUtil.class);
	
	//cookie保存路径
	private final static String DEFAULT_PATH = "/";

	/**
	 * 根据Cookie名称得到Cookie的值，没有返回Null
	 * @param request   请求
	 * @param name      key值
	 * @return {String} Cookie的值
	 * @author: zhulm
	 * @date: 2013-10-26 上午9:19:00
	 */
	public static String getCookieValue(HttpServletRequest request, String name)
	{	
		try {			
			final Cookie[] cookies = request.getCookies();		
			if(ArrayUtils.isEmpty(cookies) || StringUtils.isBlank(name)){
				return null;
			}
			for(final Cookie coo : cookies){
				if(coo.getName().equals(name)){
					return coo.getValue().trim(); //URLDecoder.decode(coo.getValue(), Encoding.UTF8.type).trim();
				}
			}
		} catch (Exception e) {
			logger.error("fail to getCookieValue, ex=" + e.getMessage(), e);
		}
		return null;
	}
	
	public static final Map<String, String> getAllCookieValues(HttpServletRequest request)
	{	
		final Map<String, String> result = new HashMap<String, String>();
		try {			
			final Cookie[] cookies = request.getCookies();		
			if(ArrayUtils.isNotEmpty(cookies)){
				for(final Cookie coo : cookies){
					result.put(coo.getName(), coo.getValue().trim());//URLDecoder.decode(coo.getValue(), Encoding.UTF8.type).trim());
				}
			}
		} catch (Exception e) {
			logger.error("fail to getCookieValues, ex=" + e.getMessage(), e);
		}
		return result;
	}
	
	/** 
	 * 删除指定Cookie  
     * @param response  
     * @param request  
     * @param name  
     */
    public static void removeCookies(HttpServletResponse response, HttpServletRequest request, 
    		final String domain, String[] names)  
    {  
		final Cookie[] cookies = request.getCookies();		
		if(ArrayUtils.isEmpty(cookies) || ArrayUtils.isEmpty(names)){
			return;
		}
		final Set<String> nameSet = CollectionUtils.toSet(names);
		for(final Cookie cookie : cookies)
		{
			if(nameSet.contains(cookie.getName()))
			{
				cookie.setPath("/");  
		        cookie.setValue("");  
		        cookie.setMaxAge(0); 
		        cookie.setDomain(domain);
		        response.addCookie(cookie);  
			}
		}
    } 
	
    public static void removeCookie(HttpServletResponse response, HttpServletRequest request, 
    		final String domain, String name)  
    {  
		final Cookie[] cookies = request.getCookies();		
		if(ArrayUtils.isEmpty(cookies)){
			return;
		}
		for(final Cookie cookie : cookies)
		{
			if(cookie.getName().equals(name))
			{
				cookie.setPath("/");  
		        cookie.setValue("");  
		        cookie.setMaxAge(0); 
		        cookie.setDomain(domain);
		        response.addCookie(cookie);  
			}
		}
    }
    
	/**
	 * 添加一条新的Cookie信息，可以设置其最长有效时间
	 * @param name		key值
	 * @param value     value值(未编码)
	 * @param maxAge    最长时间（秒）
	 * @param path      保存路劲
	 * @param response  响应
	 * @author: zhulm
	 * @date: 2013-10-26 上午8:59:53
	 */
	public static void saveCookie(String name, String value, int maxAge, String path,  HttpServletResponse response, 
			final String domain)
	{
		try {
			final Cookie cookie = new Cookie(name, (value == null ? "" : value.trim()));//RequestUtil.encodeURLParam(value, value).trim()
			cookie.setMaxAge(maxAge != 0 ? maxAge : Seconds.OneWeek.seconds);
			cookie.setPath(path != null ? path : DEFAULT_PATH);
			cookie.setDomain(domain);
			response.addCookie(cookie);			
		} catch (Exception e) {
			logger.error("fail to saveCookie, ex=" + e.getMessage(), e);
		}
	}
	
	/************************
	 * 读取session Id
	 * @param request
	 * @param response
	 * @return
	 */
	public static final String readSessionId(final HttpServletRequest request, final HttpServletResponse response)
	{
		String sessionId = request.getParameter(CookieField.SessionId.name);
		if(StringUtils.isNotBlank(sessionId)){
			return sessionId;
		}
		sessionId = CookieUtil.getCookieValue(request, CookieField.SessionId.name);
		if(StringUtils.isNotBlank(sessionId)){
			return sessionId;
		}
		
		final String random = MD5SignUtil.generateRandomKey("1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ",32);
		sessionId =  random.substring(0, 4) + String.valueOf(System.currentTimeMillis()) + random.substring(4);
		CookieUtil.saveCookie(CookieField.SessionId.name, sessionId,  Seconds.OneYear.seconds, null, response, request.getServerName());
		return sessionId;
	}
}
