/**
 * 
 */
package com.sas.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.util.keywordsearch.SearchAlgorithm;

/**
 * 舆情敏感词检测
 * @author Administrator
 *
 */
public class UGCForbidWordUtil {

	protected static final Logger logger = Logger.getLogger(UGCForbidWordUtil.class);
	
	/***************
	 * 所有UGC的敏感词列表
	 */
	private static SearchAlgorithm forbiddenKeywordSearchAlgorithm = null;

	/*************************************短信黑词***************************************************
	 * 获取所有黑词
	 * @return
	 */
	private static final SearchAlgorithm getForbiddenKeywordSearchAlgorithmEngine(){
		if(forbiddenKeywordSearchAlgorithm != null){
			return forbiddenKeywordSearchAlgorithm;
		}
		synchronized(UGCForbidWordUtil.class)
		{
			if(forbiddenKeywordSearchAlgorithm != null){
				return forbiddenKeywordSearchAlgorithm;
			}
			
			BufferedReader reader = null;
			try{
				final Set<String> allwords = new HashSet<String>();
				reader = new BufferedReader(new InputStreamReader(SasMenuActivityAddressTagUtil.class.getResourceAsStream("/core/ugc_forbid_words.txt"), Encoding.UTF8.type));
				String line = null;
				while((line = reader.readLine()) != null){
					if(StringUtils.isNotBlank(line)){
						allwords.add(line.trim());
					}
				}
				forbiddenKeywordSearchAlgorithm = new SearchAlgorithm(true);
				forbiddenKeywordSearchAlgorithm.setKeywords(allwords.toArray(new String[allwords.size()]));
				forbiddenKeywordSearchAlgorithm.buildAlgorithm();
				logger.error("Success reading read forbidden keywords: count="+ allwords.size());	
			}catch(Exception ex){
				logger.error("Fail to read forbidden keywords: ex="+ex.getMessage(), ex);	
			}finally{
				IOUtil.closeReaderWithoutException(reader);
			}
		}
		return forbiddenKeywordSearchAlgorithm;		
	}
	
	/******************
	 * 搜索所有敏感词列表
	 * @param content
	 * @return
	 */
	public static final List<String> filterForbiddenWords(final String content){
		if(StringUtils.isBlank(content)){
			return null;
		}
		return UGCForbidWordUtil.getForbiddenKeywordSearchAlgorithmEngine().SearchKeyWords(content);
	}

}
