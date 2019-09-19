/**
 * 
 */
package com.sas.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;

/**
 * 这里放置了所有我们用到的url请求参数，为了避免在微信登录时， 请求参数过长，在这里进行过滤
 * @author Administrator
 *
 */
public class RequestParamNameUtil {

	protected static final Logger logger = Logger.getLogger(RequestParamNameUtil.class);
	
	/***************
	 * 白名单的url名字列表
	 */
	private static Set<String> whiteParamNameSet = null;
	
	private static Set<String> blackParamNameSet = null;

	/*************************************参数名字***************************************************
	 * 获取白名单词
	 * @return
	 */
	private static final Set<String> getWhiteParamNameSet()
	{
		if(whiteParamNameSet != null){
			return whiteParamNameSet;
		}
		synchronized(RequestParamNameUtil.class)
		{
			if(whiteParamNameSet != null){
				return whiteParamNameSet;
			}
			
			BufferedReader reader = null;
			try{
				final Set<String> allwords = new HashSet<String>();
				reader = new BufferedReader(new InputStreamReader(SasMenuActivityAddressTagUtil.class.getResourceAsStream("/core/request_url_param_name_all.txt"), Encoding.UTF8.type));
				String line = null;
				while((line = reader.readLine()) != null){
					if(StringUtils.isNotBlank(line)){
						allwords.add(line.trim().toLowerCase());
					}
				}
				whiteParamNameSet = allwords;
				logger.error("Success reading White Param Names: count="+ allwords.size());	
			}catch(Exception ex){
				logger.error("Fail to read White Param Names: ex="+ex.getMessage(), ex);	
			}finally{
				IOUtil.closeReaderWithoutException(reader);
			}
		}
		return whiteParamNameSet;		
	}

	/*******************
	 * 获取黑名单词
	 * @return
	 */
	private static final Set<String> getBlackParamNameSet()
	{
		if(blackParamNameSet != null){
			return blackParamNameSet;
		}
		synchronized(RequestParamNameUtil.class)
		{
			if(blackParamNameSet != null){
				return blackParamNameSet;
			}
			final Set<String> allwords = new HashSet<String>();
			allwords.add("from");
			allwords.add("isappinstalled");
			allwords.add("sukey");
			allwords.add("pass_ticket");
			allwords.add("scene");
			allwords.add("sn");
			allwords.add("srcid");
			blackParamNameSet = allwords;
		}
		return blackParamNameSet;		
	}

	/****************
	 * 处理回调的url
	 * @param target
	 * @return
	 */
	public static final String processThirdPartCallbackTargetURL(String target)
	{
		if(StringUtils.isBlank(target)){
			return target;
		}
		target = target.replaceAll("(/m/)", "/").replaceAll("(%2fm%2f)", "%2f").replaceAll("(%2Fm%2F)", "%2F"); //删除/m
		final boolean isEncoded = target.indexOf("?") < 0;
		final String[] urlParts = target.split("(\\?)|(%3f)|(%3F)");
		if(urlParts.length < 2){
			return target;
		}
		final boolean isAndCharReplaced = urlParts[1].indexOf(DividerChar.WeChatAndChars.chars) > -1;
		final String[] paramParts = urlParts[1].split("(&)|(%26)|(" + DividerChar.WeChatAndChars.chars + ")");
		final StringBuilder result = new StringBuilder(urlParts[0] + (isEncoded ? "%3F" : "?"));
		for(int i=0; i<paramParts.length; i++)
		{
			if(StringUtils.isBlank(paramParts[i])){
				continue;
			}
			if(i > 0){
				if(isAndCharReplaced){
					result.append(DividerChar.WeChatAndChars.chars);
				}else{
					result.append(isEncoded ? "%26" : "&");
				}
			}
			final String[] params = paramParts[i].split("(=)|(%3D)|(%3d)");
			if(RequestParamNameUtil.isParamNameSupport(params[0]))
			{
				result.append(params[0]);
				if(params.length > 1){
					result.append(isEncoded ? "%3D" : "=");
					result.append(params[1]);
				}
			}			
		}
		return result.toString();
	}
	
	/*************
	 * 该参数名字是否支持
	 * @param name
	 * @return
	 */
	private static final boolean isParamNameSupport(String name){
		if(StringUtils.isBlank(name)){
			return false;
		}
		name = name.toLowerCase().trim();
		return !RequestParamNameUtil.getBlackParamNameSet().contains(name)
				&& RequestParamNameUtil.getWhiteParamNameSet().contains(name);
	}
	
	/**************
	 * 老方法已经弃用
	 * @param target
	 * @return
	 */
	private static final String filterWeixinParams(final String target){
		return (target == null) ? target : target.replaceAll("(\\&*from=[^&# ]+)|(\\&*FROM=[^&# ]+)|(\\&*ISAPPINSTALLED=[^&# ]+)|(\\&*isappinstalled=[^&# ]+)"
				+ "|(%.{1,3}from%.{1,3}[^%]+)|(%.{1,3}FROM%.{1,3}[^%]+)|(%.{1,3}ISAPPINSTALLED%.{1,3}[^%]+)|(%.{1,3}isappinstalled%.{1,3}[^%]+)" +
				"|(\\&*sukey=[^&# ]+)|(\\&*SUKEY=[^&# ]+)|(\\&*sn=[^&# ]+)|(\\&*SN=[^&# ]+)|(\\&*scene=[^&# ]+)|(\\&*SCENE=[^&# ]+)|(\\&*pass_ticket=[^&# ]+)|(\\&*PASS_TICKET=[^&# ]+)|(\\&*SRCID=[^&# ]+)|(\\&*srcid=[^&# ]+)" +
				"|(%.{1,3}sukey%.{1,3}[^%]+)|(%.{1,3}SUKEY%.{1,3}[^%]+)|(%.{1,3}sn%.{1,3}[^%]+)|(%.{1,3}SN%.{1,3}[^%]+)|(%.{1,3}scene%.{1,3}[^%]+)|(%.{1,3}SCENE%.{1,3}[^%]+)|(%.{1,3}pass_ticket%.{1,3}[^%]+)|(%.{1,3}PASS_TICKET%.{1,3}[^%]+)|(%.{1,3}SRCID%.{1,3}[^%]+)|(%.{1,3}srcid%.{1,3}[^%]+)" , "");
	}
	
	public static final void main(String[] agrs){
		//ukey=a76cdd086edb4fce19cbf4c2b041975292606ec15ce6f090d9d791cff7fc1f4cd36c8309757c3f179778f0bd2e84cb4d
        //还是这个idx=1&sn=a345052f455cf9e68e0ab064ee08a24d&scene=1&srcid=0310LisSwdtjPbJ7XJtykdFX&pass_ticket=0%2B2XeGspLNG3SAGtvQd4vK3%2F38FDrTHGQUuilTipRfVp9WOUS6MfU%2F5tvtM1Z1c7#rd
		System.out.println(processThirdPartCallbackTargetURL("http://hz.biezhaila.com/m/event?actId=950&from=singlemessage&isappinstalled=0&sukey=a76cdd086edb4fce19cbf4c2b041975292606ec15ce6f090d9d791cff7fc1f4cd36c8309757c3f179778f0bd2e84cb4d&idx=1&sn=a345052f455cf9e68e0ab064ee08a24d&scene=1&srcid=0310LisSwdtjPbJ7XJtykdFX&pass_ticket=0%2B2XeGspLNG3SAGtvQd4vK3%2F38FDrTHGQUuilTipRfVp9WOUS6MfU%2F5tvtM1Z1c7"));
		System.out.println(processThirdPartCallbackTargetURL("http%3A%2F%2Fhz.biezhaila.com%2Fm%2Fevent%3FactId%3D956%26from%3Dsinglemessage%26isappinstalled%3D0%26sukey%3Da76cdd086edb4fce19cbf4"));
	}
}
