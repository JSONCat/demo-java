package com.sas.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sas.core.constant.CommonConstant;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;

import com.sas.core.constant.AuthorityConstant.UserAccessRole;
import com.sas.core.constant.CommonConstant.BinaryState;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.CommonConstant.ErrorCode;
import com.sas.core.constant.CommonConstant.ModelMsgState;
import com.sas.core.constant.CommonConstant.RequestField;
import com.sas.core.constant.SasConstant.SasLanguage;
import com.sas.core.constant.ThirdPartConstant.ThirdPartAPP;
import com.sas.core.constant.TransactionConstant.PayResultMessage;
import com.sas.core.exception.ServerUnknownException;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasExt;
import com.sas.core.util.keywordsearch.SearchAlgorithm;
import com.sas.core.util.meta.SasUtil;

/**
 * request Util
 * @author zhulm
 */
public class RequestUtil{
	
	private static final Logger logger = Logger.getLogger(RequestUtil.class);

	//需要过滤的一些url信息
	private static SearchAlgorithm noFilterURLPatternSearchAlgorithm = null; //直接跳过不处理的url
	
	private static SearchAlgorithm noAuthorityRLPatternSearchAlgorithm = null; //不用授权的url匹配
	
	private static SearchAlgorithm attackAuthorityURLPatternSearchAlgorithm = null; //是否是攻击的信息
	
	private static SearchAlgorithm seoSpriderHeaderPatternSearchAlgorithm = null; //是否搜索引擎的头信息
	
	private static SearchAlgorithm weixinBrowserHeaderPatternSearchAlgorithm = null; //微信浏览器的头信息
	
	private static SearchAlgorithm marketToolRequestURLPatternSearchAlgorithm = null; //是否是营销工具的请求
	
	private static SearchAlgorithm ajaxOrWxAppRequestURLPatternSearchAlgorithm = null; //是否是ajax或微信小程序请求

	/****************
	 * 获取是否是ajax或微信小程序请求
	 * @return
	 */
	public static final SearchAlgorithm getAjaxOrWxAppRequestURLPatternSearchAlgorithm(){
		if(ajaxOrWxAppRequestURLPatternSearchAlgorithm == null)
		{
			synchronized(RequestUtil.class)
			{
				if(ajaxOrWxAppRequestURLPatternSearchAlgorithm == null)
				{
					ajaxOrWxAppRequestURLPatternSearchAlgorithm = new SearchAlgorithm(true);
					//不进行权限判断的后缀列表
					for(final String ext : new String[]{"/rest/", "/rest/wx-app"}){
						ajaxOrWxAppRequestURLPatternSearchAlgorithm.addKeyword(ext);
					}
					ajaxOrWxAppRequestURLPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		return ajaxOrWxAppRequestURLPatternSearchAlgorithm;
	}
	
	/****************
	 * 获取微信浏览器的头信息算法信息
	 * @return
	 */
	public static final SearchAlgorithm getWeixinBrowserHeaderPatternSearchAlgorithm(){
		if(weixinBrowserHeaderPatternSearchAlgorithm == null)
		{
			synchronized(RequestUtil.class)
			{
				if(weixinBrowserHeaderPatternSearchAlgorithm == null)
				{
					weixinBrowserHeaderPatternSearchAlgorithm = new SearchAlgorithm(false);
					//不进行权限判断的后缀列表
					for(final String ext : new String[]{"MicroMessenger", "micromessenger", "MICROMESSENGER"}){
						weixinBrowserHeaderPatternSearchAlgorithm.addKeyword(ext);
					}
					weixinBrowserHeaderPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		return weixinBrowserHeaderPatternSearchAlgorithm;
	}
	
	
	/****************
	 * 获取不用过滤的url算法信息
	 * @return
	 */
	public static final SearchAlgorithm getAttackURLPatternSearchAlgorithm(){
		if(attackAuthorityURLPatternSearchAlgorithm == null)
		{
			synchronized(RequestUtil.class)
			{
				if(attackAuthorityURLPatternSearchAlgorithm == null)
				{
					attackAuthorityURLPatternSearchAlgorithm = new SearchAlgorithm(false);
					//不进行权限判断的后缀列表
					for(final String ext : new String[]{".php", ".asp", ".jsp"}){
						attackAuthorityURLPatternSearchAlgorithm.addKeyword(ext.toLowerCase());
					}
					attackAuthorityURLPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		return attackAuthorityURLPatternSearchAlgorithm;
	}
	
	/****************
	 * 获取不用过滤的url算法信息
	 * @return
	 */
	public static final SearchAlgorithm getNoFilterURLPatternSearchAlgorithm(){
		if(noFilterURLPatternSearchAlgorithm == null)
		{
			synchronized(RequestUtil.class)
			{
				if(noFilterURLPatternSearchAlgorithm == null)
				{
					noFilterURLPatternSearchAlgorithm = new SearchAlgorithm(true);
					//不进行权限判断的后缀列表
					for(final String ext : new String[]{".css", ".js", ".html", ".htm", ".jpg", ".jpeg", ".bmp",
										".png", ".gif", ".doc", ".ppt", ".docx", ".pptx", ".txt",  
										".xls", ".xlsx", ".pdf", ".psd", ".ico", ".xml", ".php", ".woff", ".woff2",
										".apk", "/captcha", "/monitor", "/hessianservice", "/sitemap", "/file-download"
						}){
						noFilterURLPatternSearchAlgorithm.addKeyword(ext.toLowerCase());
					}
					noFilterURLPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		return noFilterURLPatternSearchAlgorithm;
	}

	/****************
	 * 获取不用过滤的url算法信息
	 * @return
	 */
	public static final SearchAlgorithm getNoAuthorityURLPatternSearchAlgorithm(){
		if(noAuthorityRLPatternSearchAlgorithm == null)
		{
			synchronized(RequestUtil.class)
			{
				if(noAuthorityRLPatternSearchAlgorithm == null)
				{
					noAuthorityRLPatternSearchAlgorithm = new SearchAlgorithm(true);
					//不进行权限判断的后缀列表
					noAuthorityRLPatternSearchAlgorithm.addKeyword("/error");
					noAuthorityRLPatternSearchAlgorithm.addKeyword("/upload");
					noAuthorityRLPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		return noAuthorityRLPatternSearchAlgorithm;
	}
	
	/*********
	 * 是否是营销工具的请求
	 * @return
	 */
	private static final SearchAlgorithm getMarketToolRequestURLPatternSearchAlgorithm(){
		if(marketToolRequestURLPatternSearchAlgorithm == null)
		{
			synchronized(RequestUtil.class)
			{
				if(marketToolRequestURLPatternSearchAlgorithm == null)
				{
					marketToolRequestURLPatternSearchAlgorithm = new SearchAlgorithm(true);
					marketToolRequestURLPatternSearchAlgorithm.addKeyword("market");
					marketToolRequestURLPatternSearchAlgorithm.addKeyword("bargain");
					marketToolRequestURLPatternSearchAlgorithm.addKeyword("group");
					marketToolRequestURLPatternSearchAlgorithm.addKeyword("luckdraw");
					marketToolRequestURLPatternSearchAlgorithm.addKeyword("draw");
					marketToolRequestURLPatternSearchAlgorithm.addKeyword("game");
					marketToolRequestURLPatternSearchAlgorithm.addKeyword("plant");					
					marketToolRequestURLPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		return marketToolRequestURLPatternSearchAlgorithm;
	}
	
	public static final boolean isMarketToolPageURL(final String url)
	{
		return RequestUtil.getMarketToolRequestURLPatternSearchAlgorithm().SearchKeyWords(url).size() > 1;
	}
	
	/**
	 * 获取真实ip地址
	 * @param request
	 * @return
	 */
	public static final String getUserIPFromRequest(final HttpServletRequest request)
	{
		if(request == null){
			return null;
		}
		String ip = request.getHeader("X-Real-IP");		
		if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)){
			return ip;
		}
		ip = request.getHeader("x-forwarded-for");
		if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)){
			return ip;
		}
		ip = request.getHeader("Proxy-Client-IP");
		if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)){
			return ip;
		}
		ip = request.getHeader("WL-Proxy-Client-IP");
		if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)){
			return ip;
		}
		return request.getRemoteAddr();
	}
	
	
	/**
	 * 获取完整的server地址
	 * <p> 
	 * 	如: http://www.saihuitong.com:80/ 
	 * </p>
	 * @param request
	 * @return
	 */
	public static final String getHomePageURLWithPort(final HttpServletRequest request){
		return getHomePageURLWithoutPort(request) + ":" + request.getServerPort();
	}
	
	/**
	 * 获取完整的server名称.无端口号
	 * <p>
	 * 	如: http://www.lvduoduo.com
	 * </p>
	 * @param request
	 * @return
	 */
	public static String getHomePageURLWithoutPort(final HttpServletRequest request){
		return request.getScheme() + "://" + RequestUtil.getCurrentDomain(request);
	}
	
	
//	/**
//	 * 将request中的参数转化成map
//	 * queryString解析使用
//	 * @return
//	 */	
//	public static final Map<String,String> getQueryParametersFromRequest(final HttpServletRequest request){
//		final Map<String, String> result = new HashMap<String, String>();
//		String query = request.getQueryString();
//		if(StringUtils.isBlank(query)){
//			return result;
//		}
//		String realQuery = "";
//		try {
//			realQuery = URLDecoder.decode(query, Encoding.UTF8.type);
//		} catch (Exception e) {
//			logger.error("Fail to decode request parameters to utf-8, ex="+e.getMessage(), e);
//			return result;
//		}
//		final String params[] = realQuery.split("&");
//		if(ArrayUtils.isEmpty(params)){
//			return result;
//		}
//		for(final String param : params)
//		{
//			if(StringUtils.isNotBlank(param) ){
//				final int index = param.indexOf("=");			
//				if(index > 0 && index < (param.length()-1)){
//					result.put(param.substring(0, index).trim(), param.substring(index+1).trim());
//				}else{
//					result.put(param.trim(),"");
//				}
//			}
//		}
//		return result;
//	}

	/**************
	 * 重定向一个主机
	 * @param url
	 */
	public static final String getCurrentURIWithParam(final HttpServletRequest request,
			final String otherParam, final boolean needEncode)
	{
		final StringBuilder url = new StringBuilder("http://" + request.getServerName());
		final int port = request.getServerPort();
		if(port != 80){
			url.append(":" + port);
		}
		url.append(request.getRequestURI());
		final String queryString = request.getQueryString();
		if(StringUtils.isNotBlank(queryString)){
			url.append("?" + queryString + (StringUtils.isNotBlank(otherParam) ? "&" + otherParam : ""));
		}else if(StringUtils.isNotBlank(otherParam)){
			url.append("?" + otherParam);
		}
		if(needEncode){
			return HtmlUtil.encodeParam(url.toString(), Encoding.UTF8);
		}else{
			return url.toString();
		}
	}
	
	public static final String getCurrentURIWithParam(final HttpServletRequest request, final String domain, 
			final String otherParam, final boolean needEncode)
	{
		final StringBuilder url = new StringBuilder("http://" + domain);
		final int port = request.getServerPort();
		if(port != 80){
			url.append(":" + port);
		}
		url.append(request.getRequestURI());
		final String queryString = request.getQueryString();
		if(StringUtils.isNotBlank(queryString)){
			url.append("?" + queryString + (StringUtils.isNotBlank(otherParam) ? "&" + otherParam : ""));
		}else if(StringUtils.isNotBlank(otherParam)){
			url.append("?" + otherParam);
		}
		if(needEncode){
			return HtmlUtil.encodeParam(url.toString(), Encoding.UTF8);
		}else{
			return url.toString();
		}
	}
	
	/**************
	 * 重定向一个主机
	 * @param url
	 */
	public static final void redirectToHost(final HttpServletRequest request, final HttpServletResponse response,
			String host, final boolean usingCurrentURI)
	{
		final int port = request.getServerPort();
		try {
			if(!RequestUtil.isStartWithHttpOrHttps(host)){
				host = "http://" + host + (port != 80 ?  (":"+port) : "");
			}
			if(usingCurrentURI){
				final String queryString = request.getQueryString();
				if(StringUtils.isNotBlank(queryString)){
					response.sendRedirect(host + request.getRequestURI() + "?" + queryString);
				}else{
					response.sendRedirect(host + request.getRequestURI());	
				}
			}else{
				response.sendRedirect(host);
			}
		} catch (Exception ex) {
			logger.error("Fail to redirect to host:" + host, ex);
			throw new ServerUnknownException("Fail to redirect to host:" + host, ex);
		}
	}
	
	/**************
	 * 跳转到某一个url
	 * @param request
	 * @param response
	 * @param host
	 * @param path
	 */
	public static final void redirectToURL(final HttpServletRequest request, final HttpServletResponse response,
			String host, String path){
		final int port = request.getServerPort();
		try {
			if(StringUtils.isBlank(path) || !path.startsWith("/")){
				response.sendRedirect("http://" + host + (port != 80 ?  (":"+port) : "")
						+ "/" + path);
			}else{
				response.sendRedirect("http://" + host + (port != 80 ?  (":"+port) : "")
						+ path);
			}
		} catch (IOException ex) {
			logger.error("Fail to redirect to host:" + host + ", path=" + path, ex);
			throw new ServerUnknownException("Fail to redirect to host:" + host + ", path=" + path, ex);
		}		
	}

	/******************
	 * 对链接进行重定向到登录页
	 * @param request
	 * @param response
	 * @param loginUrl
	 * @param isWap
	 */
	public static final String redirectToNormalWebLoginPage4Spring(final HttpServletRequest request){
		return RequestUtil.redirectToWebLoginPage4Spring(request, SasUtil.createLoginUrl(UserAccessRole.User));
	}
	
	public static final String redirectToWebLoginPage4Spring(final HttpServletRequest request, 
			final String loginUrl){
		return RequestUtil.redirectToWebLoginPage4Spring(request, loginUrl, "");
	}
	
	public static final String redirectToWebLoginPage4Spring(final HttpServletRequest request, 
			final String loginUrl, final String targetURLOtherParam){
		return "redirect:" 
				+ loginUrl 
				+ "?code=" + ErrorCode.AuthorityError.code 
				+ "&target=" + RequestUtil.getCurrentURIWithParam(request, targetURLOtherParam, true);
	}
	
	public static final String redirectToNormalWapLoginPage4Spring(final HttpServletRequest request){
		return RequestUtil.redirectToNormalWapLoginPage4Spring(request, "");
	}
	
	public static final String redirectToNormalWapLoginPage4Spring(final HttpServletRequest request,
			final String targetURLOtherParam){
		return RequestUtil.redirectToWapLoginPage4Spring(request, 
				SasUtil.createLoginUrl(UserAccessRole.User), BinaryState.No, targetURLOtherParam);
	}
	
	public static final String redirectToWapLoginPage4Spring(final HttpServletRequest request,
			final String loginUrl, final BinaryState oldmode){
		return RequestUtil.redirectToWapLoginPage4Spring(request, loginUrl, oldmode, "");
	}
	
	public static final String redirectToWapLoginPage4Spring(final HttpServletRequest request,
			final String loginUrl, final BinaryState oldmode, final String targetURLOtherParam)
	{
		if(loginUrl.startsWith("/m")){
			return "redirect:" 
					+ loginUrl 
					+ "?code=" + ErrorCode.AuthorityError.code 
					+ "&target=" + RequestUtil.getCurrentURIWithParam(request, targetURLOtherParam, true)
					+ "&oldmode=" + oldmode.state;
		}else{
			return "redirect:/m" 
					+ loginUrl 
					+ "?code=" + ErrorCode.AuthorityError.code 
					+ "&target=" + RequestUtil.getCurrentURIWithParam(request, targetURLOtherParam, true)
					+ "&oldmode=" + oldmode.state;
		}
		
	}
	
	/******************
	 * 创建http url
	 * @param request
	 * @param url
	 * @return
	 */
	public static final String createHTTPURL(final HttpServletRequest request, final String url){
		final int port = request.getServerPort();
		return "http://" + request.getServerName()
				+ (port != 80 ?  (":"+port) : "")
				+ (url != null && url.startsWith("/") ? url : "/" + url);
	}
	
	/**************
	 * url重定向
	 * @param request
	 * @param response
	 * @param url
	 */
	public static final void redirectToURL(final HttpServletResponse response,
			String url){
		try {
			if(RequestUtil.isStartWithHttpOrHttps(url)){
				response.sendRedirect(url);
			}else{
				response.sendRedirect("http://" + url);
			}
		} catch (IOException ex) {
			logger.error("Fail to redirect to url:" + url, ex);
			throw new ServerUnknownException("Fail to redirect to url:" + url, ex);
		}		
	}
	
	/***************
	 * 发送错误码给客户端
	 * @param msg
	 * @param errorCode
	 */
	public static final void sendErrorResponse(final HttpServletRequest request, final HttpServletResponse response, 
			String msg, final int errorCode)
	{
		if(StringUtils.isNotBlank(msg)){
			RequestUtil.dumpRequest(request, msg + ", errorCode=" + errorCode);
		}
		try {
			response.sendError(errorCode);
		} catch (IOException ex) {
			logger.error("Fail to send error response, code=" + errorCode, ex);
			throw new ServerUnknownException("Fail to send error response, code=" + errorCode, ex);
		}
	}
	
	/***************
	 * 删除内容
	 * @param text
	 */
	public static final void outputText(final HttpServletRequest request, final HttpServletResponse response, 
			final String fileName, final String text, final Encoding encoding){
		if(text == null){
			return;
		}
		try{
			response.setHeader("Content-type", "text/html;charset=" +encoding.type);
			if(fileName != null && fileName.length() > 0){
				response.setHeader("Content-Disposition", "attachment;fileName=" + RequestUtil.encodeURLParam(fileName, fileName));
			}
			response.setCharacterEncoding(encoding.type);
			//response.addHeader("Content-Length", "" + text.length());长度不对
			final OutputStream out = response.getOutputStream();  
            //这句话的意思，使得放入流的数据是utf8格式  
			out.write(text.getBytes(encoding.type));  
			out.flush();
			out.close();
		}catch(Exception ex){
			RequestUtil.dumpRequest(request, "Fail to putput value: text=" + text, ex);
		}
	}
	
	public static final void outputTextOnly(final HttpServletRequest request, final HttpServletResponse response, 
			final String text){
		if(text == null){
			return;
		}
		try{
			final PrintWriter pw = response.getWriter();  
			pw.write(text);
			pw.flush();
			pw.close();
		}catch(Exception ex){
			RequestUtil.dumpRequest(request, "Fail to outputTextOnly: text=" + text, ex);
		}
	}
	
	/*****************
	 * 输错二进制数据
	 * @param data：数据
	 * @param contentType：内容类型， 如"image/jpg"
	 * @param needDisableBrowerCache: 是否禁用浏览器缓存， 对于二维码等需要禁用缓存
	 * @param attachmentFileName：是否是以下载文件的形式打开， 对应的文件名
	 */
	public static final void outputBinaryData(final HttpServletRequest request, final HttpServletResponse response, 
			final byte[] data, final String contentType, final boolean needDisableBrowerCache,
			final String attachmentFileName)
	{
		if(needDisableBrowerCache){
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
		}
		response.setContentType(contentType);
		if(StringUtils.isNotBlank(attachmentFileName)){
			response.addHeader("Content-Disposition", "attachment;filename=" + attachmentFileName.trim());
		}
		if(data != null)
		{
			try{
				 ServletOutputStream sos=response.getOutputStream();
				 sos.write(data);//动态生成下载的内容
				 sos.flush();
				 sos.close();
			}catch(Exception ex){
				RequestUtil.dumpRequest(request, "Fail to outputBinaryData, contentType=" + contentType 
						+ ",attachmentFileName="+attachmentFileName,  ex);
			}
		}
	}

	/**
	 * 打印request信息
	 */
	public static final void dumpRequest(final HttpServletRequest request, String msg){
		RequestUtil.dumpRequest(request, msg, null);
	}
	
	public static final void dumpRequest(final HttpServletRequest request, String msg, Throwable ex){
		final String error = "Fail to access Controller, msg=" + msg
				+ ", domain:" + RequestUtil.getCurrentDomain(request)
				+ ", URI:" + request.getRequestURI() 
				+ ", IP:"  + RequestUtil.getUserIPFromRequest(request)
				+ ", Referer:" + request.getHeader("Referer")
				+ ", queryString:" + request.getQueryString()
				+ ", mid:" + request.getParameter(RequestField.MenuId.name)
				+ ", agent=" + request.getHeader("User-Agent")
				+ ", Content-Type=" + request.getHeader("Content-Type")
				+ ", Accept-Language=" + request.getHeader("Accept-Language");
		if(ex == null){
			logger.error(error);
		}else{
			logger.error(error, ex);
		}
	}

	/****************
	 * 获取不用搜索引擎的头部规则算法信息
	 * @return
	 */
	public static final SearchAlgorithm getSeoSpriderHeaderPatternSearchAlgorithm(){
		if(seoSpriderHeaderPatternSearchAlgorithm == null)
		{
			synchronized(RequestUtil.class)
			{
				if(seoSpriderHeaderPatternSearchAlgorithm == null)
				{
					seoSpriderHeaderPatternSearchAlgorithm = new SearchAlgorithm(false);
					//不进行权限判断的后缀列表 ('bot', 'crawl', 'spider' ,'slurp', 'sohu-search', 'lycos', 'robozilla');
					for(final String ext : new String[]{"googlebot", "msnbot", "slurp", "baiduspider", "sohu-search",
							"lycos", "robozilla", "bingbot", "360spider", "easouspider",
							"yodaobot", "slurp", "crawl", "twiceler", "mediapartners-google", "sogou head spider", 
							"Sogou web spider", "surveybot", "spider", "YisouSpider"}){
						seoSpriderHeaderPatternSearchAlgorithm.addKeyword(ext.toLowerCase());
					}
					seoSpriderHeaderPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		return seoSpriderHeaderPatternSearchAlgorithm;
	}
	
	/***************
	 *判断请求是不是搜索引擎的爬虫
	 * @param request
	 * @return
	 */
	public static final boolean isSearchEngineSprider(final HttpServletRequest request) {
		String agent = (request == null) ? null : request.getHeader("User-Agent");
		if (StringUtils.isBlank(agent)) {
			return false;
		}
		return CollectionUtils.isNotEmpty(RequestUtil.getSeoSpriderHeaderPatternSearchAlgorithm().SearchKeyWords(agent.toLowerCase()));
	}
	
	/*****************
	 * 创建session id
	 * @return
	 */
	public static final String createSessionId()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	/************
	 * 获取当前domain
	 * @param request
	 * @return
	 */
	public static final String getCurrentDomain(final HttpServletRequest request){
		return request.getServerName().trim().toLowerCase();
	}
	
	/**********************
	 * 是否是手机页面
	 * @param uri
	 * @return
	 */
	public  static final boolean isWap(final String uri){
		return (uri.length() == 2 && "/m".equals(uri)) || uri.startsWith("/m/");
	}
	
	/***************
	 * 将127.0.0.1形式的IP地址转换成十进制整数，这里没有进行任何错误处理  
	 * @param strIp
	 * @return
	 */
    public static final long ipToLong(final String strIp)
    {  
    	if(StringUtils.isBlank(strIp)){
    		return 0;
    	}
        //先找到IP地址字符串中.的位置  
        final int position1 = strIp.indexOf(".");  
        final int position2 = strIp.indexOf(".", position1 + 1);  
        final int position3 = strIp.indexOf(".", position2 + 1);  
        if(position1 < 1 || position2 < 1 || position3 < 1){
        	return 0;
        }
        //将每个.之间的字符串转换成整型  
        final long[] ip = new long[4];  
        ip[0] = IdUtil.convertTolong(strIp.substring(0, position1), 0);  
        ip[1] = IdUtil.convertTolong(strIp.substring(position1+1, position2), 0);    
        ip[2] = IdUtil.convertTolong(strIp.substring(position2+1, position3), 0);    
        ip[3] = IdUtil.convertTolong(strIp.substring(position3+1), 0);   
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];  
    } 

	/**************
	 * 设置状态信息
	 * @param modelMap
	 * @param state
	 * @param info
	 */
	public static final void setModelState(final ModelMap modelMap, final Sas sas, 
			final PayResultMessage resultMessage)
	{
		if(sas == null || !SasLanguage.isEnglish(sas.getLanguageSupport())){
			RequestUtil.setModelState(modelMap, resultMessage.modelMsgState, 
					resultMessage.msg, resultMessage.state);
		}else{
			RequestUtil.setModelState(modelMap, resultMessage.modelMsgState, 
					resultMessage.englishMsg, resultMessage.state);
		}
	}
	
	//选择其他付款方式
	public static final void setModelState4OtherPayOrder(final ModelMap modelMap, final Sas sas, final SasExt ext,
			final PayResultMessage payResultMessage, final boolean isOrderUnderCensor)
	{
		if(isOrderUnderCensor){
			if(sas == null || !SasLanguage.isEnglish(sas.getLanguageSupport())){
				RequestUtil.setModelState(modelMap, payResultMessage.modelMsgState, 
						payResultMessage.msg + " 审核通过后，" + ext.getOtherPayNoteChinese(),
						payResultMessage.state);
			}else{
				RequestUtil.setModelState(modelMap, payResultMessage.modelMsgState,
						payResultMessage.englishMsg + " after censor pass, " + ext.getOtherPayNoteEnglish(),
						payResultMessage.state);
			}
		}else{
			if(sas == null || !SasLanguage.isEnglish(sas.getLanguageSupport())){
				RequestUtil.setModelState(modelMap, payResultMessage.modelMsgState, 
						payResultMessage.msg + " " + ext.getOtherPayNoteChinese(), payResultMessage.state);
			}else{
				RequestUtil.setModelState(modelMap, payResultMessage.modelMsgState,
						payResultMessage.englishMsg + " " + ext.getOtherPayNoteEnglish(), 
						payResultMessage.state);
			}
		}
	}
	
	public static final void setModelState(final ModelMap modelMap, 
			final ModelMsgState state, final String payResultMessage, final String payResultState)
	{
		modelMap.put("action_state", state.state);
		modelMap.put("action_state_ext", payResultState);
		modelMap.put("action_msg", payResultMessage);
	}
	
	/**********
	 * 创建查询参数
	 * @param request
	 * @return
	 */
	public static final String createQueryString(final HttpServletRequest request) {
		final String queryString = request.getQueryString();
		if (StringUtils.isBlank(queryString)) {
			return "";
		} else {
			return "?" + queryString;
		}
	}
	
	/*****************
	 * 是否是微信浏览器
	 * @param request
	 * @return
	 */
	
	public static final boolean isWeiXin(final HttpServletRequest request){
		final String ua = request == null ? null : request.getHeader("user-agent");
		return ua != null && CollectionUtils.isNotEmpty(RequestUtil.getWeixinBrowserHeaderPatternSearchAlgorithm().SearchKeyWords(ua));
	}
	
	/*****************
	 * 是否是微信小程序浏览器
	 * @param request
	 * @return
	 */
	
	public static final boolean isWeiXinMiniProgram(final HttpServletRequest request){
		final String param = request.getParameter(CommonConstant.ModelMapFields.BrowserType.field);
		return param != null && "miniprogram".equals(param);
	}
	
	
	/***************
	 * 添加http头部到url里面
	 * @param url
	 * @return
	 */
	public static final boolean isStartWithHttpOrHttps(String url){
		if(url == null || url.length() < 1){
			return false;
		}
		url = url.trim().toLowerCase();
		return url.startsWith("http://") || url.startsWith("https://");
	}
	
	/**********
	 * 删除http头部
	 * @param url
	 * @return
	 */
	public static final String removeHttpOrHttps(String url){
		if(url == null || url.length() < 1){
			return "/";
		}
		return url.trim().replaceAll("(http://)|(https://)|(HTTP://)|(HTTPS://)", "");
	}
	
	/*********
	 * 删除域名和http头部
	 * @param url
	 * @return
	 */
	public static final String removeDomainAndHttpOrHttps(String url){
		url = RequestUtil.removeHttpOrHttps(url);
		if(url.length() < 2){
			return url;
		}
		//考虑: wb.360paobu.com/admin/market/bargains?mid=-8945 以及admin/market/bargains?mid=-8945
		final int slashStart = url.indexOf("/");
		final int pointStart = url.indexOf(".");
		if(pointStart >= 0 && (pointStart < slashStart || slashStart < 0)){//有域名的情况
			url = slashStart < 0 ? "/" : url.substring(slashStart);
			if(url.length() < 1){
				return "/";
			}
			return url;
		}else if(url.startsWith("/")){//正常
			return url;
		}else{//相对路径
			return "/" + url;
		}
	}

	/***************
	 * 添加http头部到url里面
	 * @param url
	 * @return
	 */
	public static final String checkAndInsertHttp2URL(String url){
		if(url == null || url.length() < 1){
			return url;
		}
		if(!url.startsWith("http://") && !url.startsWith("https://")&&!url.startsWith("HTTP://") && !url.startsWith("HTTPS://")){
			return "http://" + url;
		}
		return url;
	}
	
	/**************
	 * encode url参数
	 * @param param
	 * @param defaultValue
	 * @return
	 */
	public static final String encodeURLParam(final String param, final String defaultValue){
		try{
			return URLEncoder.encode(param, Encoding.UTF8.type);
		}catch(Exception ex){
			return defaultValue;
		}
	}
	
	/**************
	 * decode url参数
	 * @param param
	 * @param defaultValue
	 * @return
	 */
	public static final String decodeURLParam(final String param, final String defaultValue){
		try{
			return URLDecoder.decode(param, Encoding.UTF8.type);
		}catch(Exception ex){
			return defaultValue;
		}
	}
	
	/***********
	 * 删除非法字符
	 * @param url
	 * @return
	 */
	public static final String removeInValidURLChars(final String url){
		final StringBuilder newURL = new StringBuilder("");
		char prevChar = ':';
		//删除空格和连着的双//， 例如http://www.txsyxh.cn//data/attachment/forum/201308/14/155625qf27oty40fwnzcun.jpg
		for(final char ch : url.toCharArray())
		{
			if(ch == ' ' || ch == '　' || ch == '\t' || ch == '\n'){
				continue;
			}
			if(ch == '/' && prevChar == '/' && newURL.length() > 8){//跳过http://
				continue;
			}else{
				newURL.append(ch);
				prevChar = ch;
			}
		}
		return newURL.toString();
	}
	
	/************
	 * 编码下载的文件名称
	 * @param request
	 * @param filename
	 * @return
	 */
	public static final String encodeDownloadFilename(HttpServletRequest request, String filename) {  
        /** 
         * 获取客户端浏览器和操作系统信息 在IE浏览器中得到的是：User-Agent=Mozilla/4.0 (compatible; MSIE 
         * 6.0; Windows NT 5.1; SV1; Maxthon; Alexa Toolbar) 
         * 在Firefox中得到的是：User-Agent=Mozilla/5.0 (Windows; U; Windows NT 5.1; 
         * zh-CN; rv:1.7.10) Gecko/20050717 Firefox/1.0.6 
         */  
        String agent = request.getHeader("USER-AGENT");  
        try {  
            if(agent != null){  
                if (agent.indexOf("MSIE") != -1 || agent.indexOf("Trident") != -1) {  
                    return URLEncoder.encode(filename, "UTF-8");  
                } else if (agent.indexOf("Mozilla") != -1) {  
                    return MimeUtility.encodeText(filename, "UTF-8", "B");  
                }  
            }  
            return filename;  
        } catch (Exception ex) {  
            return filename;  
        }  
    } 
	
	/***************
	 * 利用微博地址生成短地址
	 * @param urlNoEncode
	 * @return
	 */
	public static final String generateShortURLByWeibo(String urlNoEncode)
	{
		urlNoEncode = urlNoEncode.trim();
		final String sourceURL = RequestUtil.encodeURLParam(urlNoEncode, urlNoEncode);
		final String text = IOUtil.readTextFromHttpURL("http://api.t.sina.com.cn/short_url/shorten.json?source=" + ThirdPartAPP.WEIBO_Saihuitong.appid
				+ "&url_long=" + sourceURL, 2);
		if(StringUtils.isBlank(text)){
			return urlNoEncode;
		}
		final String[] parts = text.split("\"");
		for(int i=0 ;i<parts.length; i++){
			if("url_short".equalsIgnoreCase(parts[i])){
				return parts[i+2];
			}
		}
		return urlNoEncode;
	}
	
	public static final void main(String[] args)
	{
		System.out.println(generateShortURLByWeibo("http://www.baidu.com"));
	}
}
