package com.sas.core.util.meta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.DomainConstant;
import com.sas.core.constant.DomainConstant.DomainState;
import com.sas.core.constant.DomainConstant.SpecialDomain;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.meta.Domain;
import com.sas.core.util.CollectionUtils;
import com.sas.core.util.IOUtil;
import com.sas.core.util.IdUtil;
import com.sas.core.util.ValidatorUtil;
import com.sas.core.util.keywordsearch.SearchAlgorithm;

/***********
 * 域名相关的util
 * @author zhuliming
 *
 */
public class DomainUtil {
	
	private static final Logger logger = Logger.getLogger(DomainUtil.class);

	/***************
	 * 保留域名
	 */
	private static Set<String> sasRetainedSubDomains;
	
	/********
	 * 所有静态资源域名
	 */
	private static final Set<String> allStatisticResourceDomain = new HashSet<String>();
	static{
		allStatisticResourceDomain.add(DomainConstant.StatisticResourceDomain);
		allStatisticResourceDomain.add(DomainConstant.StatisticResourceDomainBieZhaiLa);
		allStatisticResourceDomain.add(DomainConstant.StatisticResourceDomainBoxhiking);
	}
	
	/*******
	 * 短地址
	 */
	private static final Set<String> allShortUrlDomain = new HashSet<String>();
	static{
		allShortUrlDomain.add("t.saihuitong.com");
		allShortUrlDomain.add("t.ese123.com");
	}
	
	//搜索域名.cn等后缀的算法
	static SearchAlgorithm domainPostFixSearchAlgorithm = null;
	
	/*****************
	 * 是否是静态资源域名
	 * @param domain
	 * @return
	 */
	public static final boolean isStatisticResourceDomain(final String domain){
		return allStatisticResourceDomain.contains(domain);
	}

	/*****************
	 * 是否是短地址域名
	 * @param domain
	 * @return
	 */
	public static final boolean isShortUrlDomain(final String domain){
		return allShortUrlDomain.contains(domain);
	}
	
	public static final boolean isStatisticResourceUrl(final String imageUrl)
	{
		return imageUrl != null && (imageUrl.contains(DomainConstant.StatisticResourceDomain)
				|| imageUrl.contains(DomainConstant.StatisticResourceDomainBieZhaiLa));
	}
	
	/******************
	 * 如果是二级域名， 则删除后缀
	 * @return
	 */
	public static final String removeSasSubDomainSuffix(String domain)
	{
		if(domain == null){
			return "";
		}
		final int sourceLength = domain.length();
		final SpecialDomain[] allSpecialDomains = SpecialDomain.values();
		for(SpecialDomain specialDomain : allSpecialDomains){
			domain = domain.replace(specialDomain.domain, "");
			if(domain.length() != sourceLength){
				break;
			}
		}
		return domain;
	}
	
	/**********************
	 * 判断是否是二级域名
	 * @param domain
	 * @return
	 */
	public static final boolean isSubDomain(final String domain)
	{
		if(domain == null){
			return false;
		}
		return SpecialDomain.filterSubDomain(domain) != null;
	}
	
	/********************
	 * 是否是赛会通旗下的一级域名
	 * @param domain
	 * @return
	 */
	public static final boolean isSaihuitongToplevelDomain(final String domain){
		if(domain == null){
			return false;
		}
		return SpecialDomain.listAllTopLevelDomainNames().contains(domain);
	}	

	/*****************
	 * 过滤出域名的后缀
	 * @param domain
	 * @return
	 */
	public static final String filterDomainPostFixChars(final String domain)
	{
		if(domainPostFixSearchAlgorithm == null)
		{
			synchronized(DomainUtil.class){
				if(domainPostFixSearchAlgorithm == null){
					domainPostFixSearchAlgorithm = new SearchAlgorithm(true);
					for(final String end : DomainConstant.DomainPostFixPriorityArray){//后缀从长到短
						domainPostFixSearchAlgorithm.addKeyword(end);
					}
					domainPostFixSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		final List<String> ends = domainPostFixSearchAlgorithm.SearchKeyWords(domain);
		if(CollectionUtils.isEmpty(ends)){
			return null;
		}
		String result = null;
		for(final String end : ends)
		{
			if(! domain.endsWith(end)){//避免类似www.cnabc.com后缀在中间的情形
				continue;
			}
			if(result == null || result.length() < end.length()){
				result = end;
			}
		}
		return result;
	}
	
	/**
	 * 解析出当前域名的顶级域名， 如果不是二级域名的话， 否则返回当前域名，不帶"."
	 * @param domain
	 * @return
	 */
	public static final String parseTopLevelDomain(String domain)
	{
		domain = domain.toLowerCase();
		//获取后缀
		final String domainPostFix = DomainUtil.filterDomainPostFixChars(domain);
		if(domainPostFix == null){
			return domain; 
		}
		domain = domain.substring(0, domain.lastIndexOf(domainPostFix)); //获取前缀
		final String[] parts = domain.split("\\.");	
		if(StringUtils.isBlank(parts[parts.length - 1])){
			return domain + domainPostFix;
		}else if(parts.length == 1){
			return parts[0] + domainPostFix;
		}else{
			return "." + parts[parts.length - 1] + domainPostFix;
		}
	}

	/************************************保留的二级域名**************
	 * 获取所有保留的二级域名
	 * @return
	 */
	public static final boolean isSubDomainRetained(final String subDomain){
		if(StringUtils.isBlank(subDomain)){
			return true;
		}
		if(sasRetainedSubDomains == null){
			synchronized(SasUtil.class)
			{
				if(sasRetainedSubDomains == null){
					BufferedReader reader = null;
					try{
						final Set<String> domains = new HashSet<String>();
						reader = new BufferedReader(new InputStreamReader(SasUtil.class.getResourceAsStream("/core/sas_domain_retain_list.txt"), 
								Encoding.UTF8.type));
						String line = null;
						while((line = reader.readLine()) != null){
							if(StringUtils.isNotBlank(line)){
								domains.add(line.toLowerCase().trim());
							}
						}
						sasRetainedSubDomains = domains;
						logger.error("Success reading retained domains: count="  +sasRetainedSubDomains.size());	
					}catch(Exception ex){
						logger.error("Fail to read retained domains: ex="+ex.getMessage(), ex);	
					}finally{
						IOUtil.closeReaderWithoutException(reader);
					}
				}
			}
		}
		return sasRetainedSubDomains != null && sasRetainedSubDomains.contains(subDomain);
	}
	
	/*************
	 * 处理域名
	 * @param domain
	 * @return
	 */
	public static final BinaryEntry<String, Long> processDomainName(String domain){
		if(StringUtils.isBlank(domain)){
			return new BinaryEntry<String, Long>("", 0L);
		}
		domain = domain.trim().toLowerCase();
		long resaleShopUserId = 0L;
		if(ValidatorUtil.ResaleShopIdValidate(domain)){
			final int index = domain.indexOf('.');
			resaleShopUserId = IdUtil.convertTolong(domain.substring(4,index), 0L);
			domain = domain.substring(index+1);
		}
		//是否是二级域名
		if(!DomainUtil.isSubDomain(domain) && !domain.startsWith("www.")){
			domain = "www." + domain;
		}
		return new BinaryEntry<String, Long>(domain, resaleShopUserId);
	}
	
	public static final String processDomainNameOnly(String domain){
		if(StringUtils.isBlank(domain)){
			return "";
		}
		domain = domain.trim().toLowerCase();
		//是否是二级域名
		if(!DomainUtil.isSubDomain(domain) && !domain.startsWith("www.")){
			return "www." + domain;
		}
		return domain;
	}
	
	/******************
	 * 创建实体对应的主域名缓存中的key
	 * @param domain
	 * @return
	 */
	public static final String createObjectMainICPDomainKey(final Domain domain){
		return DomainUtil.createObjectMainICPDomainKey(domain.getObjectId(), domain.getType());
	}
	
	public static final String createObjectMainICPDomainKey(final long objectId, final char type){
		return "ICP_" + objectId + "_" + type + "_" + DomainState.AlreadyAliyunICP.name();
	}

	/**************
	 * 是否新域名优先级高
	 * @param newDomain
	 * @param oldDomain
	 * @return
	 */
	public static final boolean isNewDomainMoreImportantThanOldDomain(final String newDomain, final String oldDomain){
		if(StringUtils.isBlank(oldDomain)){
			return true;
		}
		if(StringUtils.isBlank(oldDomain) || oldDomain.equalsIgnoreCase(newDomain)){
			return false;
		}
		int indexOfNewDomain = -1;
		int indexOfOldDomain = -1;
		for(int i=0; i<DomainConstant.DomainPostFixPriorityArray.length; i++){
			if(indexOfNewDomain >= 0 && indexOfOldDomain >= 0){
				break;
			}
			if(indexOfNewDomain < 0 && newDomain.endsWith(DomainConstant.DomainPostFixPriorityArray[i])){
				indexOfNewDomain = i;
			}
			if(indexOfOldDomain < 0 && oldDomain.endsWith(DomainConstant.DomainPostFixPriorityArray[i])){
				indexOfOldDomain = i;
			}
		}
		return indexOfNewDomain > indexOfOldDomain;
	}
	
	/******************
	 * 解析出当前域名， 以及所有的二级域名[考虑了非赛会通的域名]
	 * @param domain
	 * @return
	 */
	public static final String[] parseAllPossibleCookieDomains(String domain)
	{
		domain = domain.toLowerCase();
		//获取后缀
		final String domainPostFix = DomainUtil.filterDomainPostFixChars(domain);
		if(domainPostFix == null){
			return new String[]{domain}; 
		}
		domain = domain.substring(0, domain.lastIndexOf(domainPostFix)); //获取前缀
		final String[] parts = domain.split("\\.");		
		final Set<String> result = new HashSet<String>();
		int start = 0;
		while(start < parts.length){
			final StringBuilder sb = start > 0 ? new StringBuilder(".") : new StringBuilder("");
			for(int i=start; i<parts.length; i++){
				sb.append(parts[i]);
				if(i < parts.length - 1){
					sb.append(".");
				}
			}
			result.add(sb.toString() + domainPostFix);
			start ++;
		}
		return result.toArray(new String[result.size()]);
	}
	
	/***************
	 * 所有地址的tag列表
	 */
	private static Set<String> _domainPostFixSet4QQForbidden = null;
	private static final Set<String> getAllSubDomainPostFix2AvoidQQForbidden(){
		if(_domainPostFixSet4QQForbidden == null)
		{
			synchronized(DomainUtil.class)
			{
				if(_domainPostFixSet4QQForbidden == null){
					_domainPostFixSet4QQForbidden = new HashSet<String>();
					_domainPostFixSet4QQForbidden.add("360jlb");
					_domainPostFixSet4QQForbidden.add("saihuitong");
					_domainPostFixSet4QQForbidden.add("360xiehui");
					_domainPostFixSet4QQForbidden.add("360pashan");
					_domainPostFixSet4QQForbidden.add("360paobu");
					_domainPostFixSet4QQForbidden.add("360mls");
				}
			}
		}
		return _domainPostFixSet4QQForbidden;
	}
	
	public static final boolean containsDomain2AvoidQQForbiddenAndSupportWXShare(final String domain){
		final Set<String> domainPostFixSet4QQForbidden = DomainUtil.getAllSubDomainPostFix2AvoidQQForbidden();
		final String[] arr = domain.split("\\.");
		if(arr.length < 5){
			return false;
		}
		return domainPostFixSet4QQForbidden.contains(arr[arr.length-4]) && domainPostFixSet4QQForbidden.contains(arr[arr.length-2]);
	}
	public static final String parseRealSubDomain2AvoidQQForbiddenAndSupportWXShare(final String domain)
	{
		final String[] arr = domain.split("\\.");
		if(arr.length < 5){
			return domain;
		}
		final Set<String> domainPostFixSet4QQForbidden = DomainUtil.getAllSubDomainPostFix2AvoidQQForbidden();
		if(domainPostFixSet4QQForbidden.contains(arr[arr.length-2])){
			final StringBuilder sb = new StringBuilder("");
			for(int i=0; i<arr.length; i++){
				if(i >= arr.length-2){
					break;
				}
				if(sb.length() > 0){
					sb.append(".");
				}
				sb.append(arr[i]);
			}
			return sb.toString();
		}
		return domain;
	}
	//支持微信分享
	public static final String createSubDomain2AvoidQQForbiddenAndSupportWXShare(final String subDomain){
		if(SpecialDomain.filterSubDomain2AvoidQQForbiddenAndSupportWXShare(subDomain) != null){
			return subDomain;
		}else{
			return subDomain + "." + SpecialDomain.jlb_cn.domain;
		}
	}
  
  /**
   * 替换url中参数的值
   *
   * @param url
   * @param key
   * @param value
   * @return
   */
	public static final String regularReplaceUrlParam(final String url, final String key, final String value){
	  if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(value)) {
			return url.replaceAll("(" + key + "=[^&]*)", key + "=" + value);
		}
		return url;
  }
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		DomainUtil.parseRealSubDomain2AvoidQQForbiddenAndSupportWXShare("tdc.360qixing.cn.360jlb.cn");
	}

}