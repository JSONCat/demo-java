package com.sas.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sas.core.constant.MenuConstant.MenuConfigDataField;
import com.sas.core.util.keywordsearch.SearchAlgorithm;

public class SasMenuLinkUtil {

	private static SearchAlgorithm scoreOrApplierQueryURLPatternSearchAlgorithm = null; //是否需要添加mid
	
	private static final SearchAlgorithm getScoreOrApplierQueryURLPatternSearchAlgorithm(){
		if(scoreOrApplierQueryURLPatternSearchAlgorithm == null)
		{
			synchronized(SasMenuLinkUtil.class)
			{
				if(scoreOrApplierQueryURLPatternSearchAlgorithm == null)
				{
					scoreOrApplierQueryURLPatternSearchAlgorithm = new SearchAlgorithm(false);
					//不进行权限判断的后缀列表
					for(final String ext : new String[]{"/event/applier", "/event/grade"}){
						scoreOrApplierQueryURLPatternSearchAlgorithm.addKeyword(ext.toLowerCase());
					}
					scoreOrApplierQueryURLPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		return scoreOrApplierQueryURLPatternSearchAlgorithm;
	}
	
	/****************
	 * 是否需要添加mid参数: 报名查询和成绩查询
	 * @return
	 */
	public static final boolean isScoreOrApplierQueryURL(final String url){
		return CollectionUtils.isNotEmpty(getScoreOrApplierQueryURLPatternSearchAlgorithm().SearchKeyWords(url));
	}
	
	/*********
	 * 是否是成绩查询的url
	 * @param url
	 * @return
	 */
	public static final boolean isScoreQueryURL(final String url){
		final List<String> keywords = getScoreOrApplierQueryURLPatternSearchAlgorithm().SearchKeyWords(url);
		if(CollectionUtils.isEmpty(keywords)){
			return false;
		}
		return keywords.get(0).indexOf("grade") >= 0;
	}
	
	public static final boolean isApplierQueryURL(final String url){
		final List<String> keywords = getScoreOrApplierQueryURLPatternSearchAlgorithm().SearchKeyWords(url);
		if(CollectionUtils.isEmpty(keywords)){
			return false;
		}
		return keywords.get(0).indexOf("applier") >= 0;
	}
	
	/*********************
	 * 解析对应的链接信息
	 * @param configData
	 * @return
	 */
	public static final String parseURLFromConfigData(final String configData)
	{
		if(StringUtils.isBlank(configData)){
			return "";
		}
		final Map<String, String> configMap = JsonUtil.getObject(configData, Map.class);
		if(configMap == null){
			return "";
		}else {
			final String url = configMap.get(MenuConfigDataField.Url.field);
			return (url == null ? "" : url);
		}
	}
	
	/**********************
	 * 替换新的url
	 * @param configData
	 * @param newURL
	 * @return
	 */
	public static final String replaceNewLinkURL(final String configData, final String newURL){
		Map<String, String> configMap = null;
		if(StringUtils.isNotBlank(configData)){
			configMap = JsonUtil.getObject(configData, Map.class);
		}
		if(configMap == null){
			configMap = new HashMap<String, String>();
		}
		configMap.put(MenuConfigDataField.Url.field, StringUtils.defaultIfBlank(newURL, ""));
		return JsonUtil.getJsonString(configMap);
	}
	
	/************
	 * 添加mid到链接
	 * @param url
	 * @param menuId
	 * @return
	 */
	public static final String addParamMid2URL(final String url, final long menuId){
		if(url == null || !SasMenuLinkUtil.isScoreOrApplierQueryURL(url)){
			return url;
		}else {
			final int paramIndex = url.indexOf('?');
			if(paramIndex >= 0){
				final String param = url.substring(paramIndex);
				return param.toLowerCase().contains("mid") ? url : url + "&mid=" + menuId;
			}else{
				return url + "?mid=" + menuId;
			}
		}
	}
}
