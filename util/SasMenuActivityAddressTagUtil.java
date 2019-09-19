/**
 * 
 */
package com.sas.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.CommonConstant.SortOrder;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.dto.ChinaProvinceCityInfo;
import com.sas.core.meta.SasMenuActivityAddress;
import com.sas.core.util.keywordsearch.SearchAlgorithm;

/**
 * 活动地址解析类
 * @author zhuliming
 *
 */
public final class SasMenuActivityAddressTagUtil {

	 protected static final Logger logger = Logger.getLogger(SasMenuActivityAddressTagUtil.class);
	 
	/***************
	 * 所有地址的tag列表
	 */
	private static SearchAlgorithm chineseAddressTagSearchAlgorithm = null;
	private static Map<String, String> chineseAddressTag2EnglishMap = null;
	
	/**
	 * 俱乐部地址标签
	 */
	private static Map<String, Set<String>> chinaProvinceCityMap = null;
	
	//国家名的中英文对照关系
	private static Map<String, String> countryChinese2EnglishNameMap = null;
	
	/****
	 * 所有省份
	 */
	private static List<String> allProvinces = null;

	/*************************************景点和地址库***************************************************
	 * 获取所有地址和景点名字列表
	 * @return
	 */
	private static final SearchAlgorithm getKeywordSearchAlgorithmEngine(){
		if(chineseAddressTagSearchAlgorithm != null){
			return chineseAddressTagSearchAlgorithm;
		}
		synchronized(SasMenuActivityAddressTagUtil.class)
		{
			if(chineseAddressTagSearchAlgorithm != null){
				return chineseAddressTagSearchAlgorithm;
			}
			
			BufferedReader reader = null;
			final Map<String, String> chinese2EnglishMap = new HashMap<String, String>(); //中文名字到英文的映射
			try{
				final Set<String> allChineseWords = new HashSet<String>();
				reader = new BufferedReader(new InputStreamReader(SasMenuActivityAddressTagUtil.class.getResourceAsStream("/core/activity_address_tag.txt"), Encoding.UTF8.type));
				String line = null;
				while((line = reader.readLine()) != null){
					if(StringUtils.isBlank(line)){
						continue;
					}
					final String array[] = line.split(DividerChar.Equal.chars);
					if(array.length < 2){
						continue;
					}
					allChineseWords.add(array[0].trim());
					chinese2EnglishMap.put(array[0].trim(), array[1].trim());
				}
				chineseAddressTagSearchAlgorithm = new SearchAlgorithm(true);
				chineseAddressTagSearchAlgorithm.setKeywords(allChineseWords.toArray(new String[allChineseWords.size()]));
				chineseAddressTagSearchAlgorithm.buildAlgorithm();
				chineseAddressTag2EnglishMap = chinese2EnglishMap;
				logger.error("Success reading read address scene tags: count="+ allChineseWords.size());	
			}catch(Exception ex){
				logger.error("Fail to read address scene tags: ex="+ex.getMessage(), ex);	
			}finally{
				IOUtil.closeReaderWithoutException(reader);
			}
		}
		return chineseAddressTagSearchAlgorithm;		
	}
	
//	public static void main(String[] args)
//	{
//		BufferedReader reader1 = null, reader2 = null;
//		try{
//			final List<String> chineseWords = new LinkedList<String>();
//			final List<String> endlishWords = new LinkedList<String>();
//			reader1 = new BufferedReader(new InputStreamReader(SasMenuActivityAddressTagUtil.class.getResourceAsStream("/core/activity_address_tag.txt"), Encoding.UTF8.type));
//			reader2 = new BufferedReader(new InputStreamReader(SasMenuActivityAddressTagUtil.class.getResourceAsStream("/core/activity_address_tag_en.txt"), Encoding.UTF8.type));
//			String line = null;
//			while((line = reader1.readLine()) != null){
//				if(StringUtils.isNotBlank(line)){
//					chineseWords.add(line.trim());
//				}
//			}
//			logger.error("chinese count="+ chineseWords.size());	
//			while((line = reader2.readLine()) != null){
//				if(StringUtils.isNotBlank(line)){
//					endlishWords.add(line.trim());
//				}
//			}
//			logger.error("english count="+ endlishWords.size());
//			final BufferedWriter writer = new BufferedWriter(new FileWriter("C:/address.txt"));
//			for(int i=0; i<endlishWords.size() ; i++)
//			{
//				writer.write(chineseWords.get(i) + "=" + endlishWords.get(i));
//				writer.newLine();
//			}
//			writer.flush();
//			writer.close();
//		}catch(Exception ex){
//			logger.error("Fail to read address scene tags: ex="+ex.getMessage(), ex);	
//		}finally{
//			IOUtil.closeReaderWithoutException(reader1);
//			IOUtil.closeReaderWithoutException(reader2);
//		}
//	}
//	
	/*****************
	 * 分析出地址中包含的地址列表
	 * @param address
	 * @return
	 */
	public static final Set<String> parseAddressAndSceneTags(final String address)
	{
		if(StringUtils.isNotBlank(address))
		{
			final List<String> list = getKeywordSearchAlgorithmEngine().SearchKeyWords(address);
			final Set<String> result = new HashSet<String>();
			if(list.size() < 2){
				if(list.size() > 0){
					result.add(list.get(0));
				}
				return result ;
			}
			//判断一个地名是否是另一个的前缀或子串:复杂度n*n/2
			final List<BinaryEntry<String, Integer>> listWithStringLength = new ArrayList<BinaryEntry<String, Integer>>(list.size());
			for(final String add : list){
				listWithStringLength.add(new BinaryEntry<String, Integer>(add, add.length()));				
			}
			CollectionUtils.sortTheList(listWithStringLength, "value", SortOrder.ASC);
			final String[] array = list.toArray(new String[list.size()]);
			int i = -1;
			for(BinaryEntry<String, Integer> v : listWithStringLength){
				array[++i] = v.getKey();
			}
			for(i=0; i<array.length; i++)
			{
				boolean isSubStringOfSomeOldAddress = false;
				for(int j=i+1; j<array.length; j++)
				{
					if(array[j].length() > array[i].length() && array[j].indexOf(array[i]) >= 0){
						isSubStringOfSomeOldAddress = true;
						break;
					}
				}
				if(!isSubStringOfSomeOldAddress){
					result.add(array[i]);
				}
			}
			return result;
		}
		return new HashSet<String>(0);
	}
	
	public static void main(String[] args)
	{
		final List<String> list = new ArrayList<String>();
		list.add("石林山");
		list.add("石林");
		list.add("石林峡ASC");
		list.add("石林峡");
		final Set<String> result = new HashSet<String>();
		//判断一个地名是否是另一个的前缀或子串:复杂度n*n/2
		final List<BinaryEntry<String, Integer>> listWithStringLength = new ArrayList<BinaryEntry<String, Integer>>(list.size());
		for(final String add : list){
			listWithStringLength.add(new BinaryEntry<String, Integer>(add, add.length()));				
		}
		CollectionUtils.sortTheList(listWithStringLength, "value", SortOrder.ASC);
		final String[] array = list.toArray(new String[list.size()]);
		int i = -1;
		for(BinaryEntry<String, Integer> v : listWithStringLength){
			array[++i] = v.getKey();
		}
		for(i=0; i<array.length; i++)
		{
			boolean isSubStringOfSomeOldAddress = false;
			for(int j=i+1; j<array.length; j++)
			{
				if(array[j].length() > array[i].length() && array[j].indexOf(array[i]) >= 0){
					isSubStringOfSomeOldAddress = true;
					break;
				}
			}
			if(!isSubStringOfSomeOldAddress){
				result.add(array[i]);
			}
		}
	}
	
	/****************
	 * 转成英文地址
	 * @param list
	 * @return
	 */
	public static final List<SasMenuActivityAddress> convert2EnglishAddress(final List<SasMenuActivityAddress> list)
	{
		if(list == null){
			return list;
		}
		if(chineseAddressTag2EnglishMap == null){
			SasMenuActivityAddressTagUtil.getKeywordSearchAlgorithmEngine();
		}
		for(final SasMenuActivityAddress address : list){
			final String english = chineseAddressTag2EnglishMap.get(address.getAddress());
			if(english != null){
				address.setAddress(english);
			}
		}
		return list;
	}
	
	/*****************************************************判断集合相等性***************************************************
	 * 判断2个集合是否相等
	 * @param add1
	 * @param add2
	 * @return
	 */
	public static final boolean isSameAddressSet(Set<String> add1, Set<String> add2)
	{
		if(add1 == null){
			add1 = new HashSet<String>();
		}
		if(add2 == null){
			add2 = new HashSet<String>();
		}
		if(add1.size() != add2.size()){
			return false;
		}
		for(String add : add1){
			if(!add2.contains(add)){
				return false;
			}
		}
		return true;
	}
	
	
	/*********************************************读取省市标签库******************************************
	 * 读取省市标签
	 * jason
	 * @return
	 */
	private static final Map<String, Set<String>> getChinaProvinceCityMap(){
		if(chinaProvinceCityMap != null){
			return chinaProvinceCityMap;
		}
		synchronized(SasMenuActivityAddressTagUtil.class)
		{
			if(chinaProvinceCityMap != null){
				return chinaProvinceCityMap;
			}
			BufferedReader reader = null;
			try{
				final Map<String,Set<String>> result = new HashMap<String,Set<String>>();
				reader = new BufferedReader(new InputStreamReader(SasMenuActivityAddressTagUtil.class.getResourceAsStream("/core/china_province_city_list.txt"), Encoding.UTF8.type));
				String line = null;
				while((line = reader.readLine()) != null){
					if(StringUtils.isBlank(line)){
						continue;
					}
					final String[] addresses = line.trim().split("=");
					if(addresses.length < 2 || StringUtils.isBlank(addresses[0])){
						logger.fatal("Error pronvince and city info: line" + line);
						continue;
					}
					final String[] citys = addresses[1].split(",");
					final Set<String> allCities = new HashSet<String>();
					for(String city: citys){
						if(StringUtils.isNotBlank(city)){
							allCities.add(city.trim());
						}
					}
					result.put(addresses[0].trim(), allCities);					
				}
				chinaProvinceCityMap = result;
				logger.error("Finish read chinese province and city info, province Count=" + result.size());	
			}catch(Exception ex){
				logger.error("Fail to read chinese province and city info: ex="+ex.getMessage(), ex);	
			}finally{
				IOUtil.closeReaderWithoutException(reader);
			}
		}
		return chinaProvinceCityMap;	
	}
	
	/**
	 * 地址转省市, 如果没找到省， 则返回null， 否则返回对象， 对象中的city可能为空窜【找不到city的话】
	 * @param address
	 * @return
	 */
	public static final ChinaProvinceCityInfo parseProvinceAndCityFromAddress(String address)
	{
		if(StringUtils.isBlank(address)){
			return null;
		}
		final Map<String, Set<String>> provinceAndCityMap = SasMenuActivityAddressTagUtil.getChinaProvinceCityMap();
		for(final Map.Entry<String, Set<String>> entry : provinceAndCityMap.entrySet())
		{
			if(address.indexOf(entry.getKey()) >= 0)
			{
				for(final String city : entry.getValue()){
					if(address.indexOf(city) >= 0){
						return new ChinaProvinceCityInfo(entry.getKey(), city);
					}
				}
				return new ChinaProvinceCityInfo(entry.getKey(), "");
			}
		}
		return null;
	}
	
	/****************
	 * 获取所有省份信息
	 * @return
	 */
	public static final List<String> listAllProvinces(){
		if(allProvinces == null){
			synchronized(SasMenuActivityAddressTagUtil.class){
				if(allProvinces == null){
					allProvinces = new LinkedList<String>();
					allProvinces.add("安徽");	allProvinces.add("北京");	allProvinces.add("重庆");	allProvinces.add("福建");
					allProvinces.add("甘肃");	allProvinces.add("广东");	allProvinces.add("广西");	allProvinces.add("贵州");
					allProvinces.add("海南");	allProvinces.add("河北");	allProvinces.add("河南");	allProvinces.add("黑龙江");
					allProvinces.add("湖北");	allProvinces.add("湖南");	allProvinces.add("吉林");	allProvinces.add("江苏");
					allProvinces.add("江西");	allProvinces.add("辽宁");	allProvinces.add("内蒙古");allProvinces.add("宁夏");					
					allProvinces.add("青海");	allProvinces.add("山东");	allProvinces.add("山西");	allProvinces.add("陕西");
					allProvinces.add("上海");	allProvinces.add("四川");	allProvinces.add("天津");	allProvinces.add("西藏");	
					allProvinces.add("云南");allProvinces.add("新疆");allProvinces.add("浙江");	allProvinces.add("台湾");
					allProvinces.add("香港");allProvinces.add("澳门");		
				}
			}
		}
		return allProvinces;
	}
	
	/***************
	 * 获取中文国家名到英文名的映射
	 * 
	 * @return
	 */
	public static final Map<String, String> getCountryChinese2EnglishNameMap(){
		if(countryChinese2EnglishNameMap != null){
			return countryChinese2EnglishNameMap;
		}
		final Map<String, String> map = new HashMap<String, String>();
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(SasMenuActivityAddressTagUtil.class.getResourceAsStream("/core/country_chinese_english_name.txt"), Encoding.UTF8.type));
			String line = null;
			while((line = reader.readLine()) != null){
				if(StringUtils.isBlank(line)){
					continue;
				}
				final String array[] = line.trim().split(DividerChar.Equal.chars);
				if(array.length < 2){
					continue;
				}
				map.put(array[0], array[1].toLowerCase());
			}
			logger.error("Success reading chinese&english country names: count="+ map.size());	
		}catch(Exception ex){
			logger.error("Fail to read chinese&english country names: ex="+ex.getMessage(), ex);	
		}finally{
			IOUtil.closeReaderWithoutException(reader);
		}		
		countryChinese2EnglishNameMap = map;
		return countryChinese2EnglishNameMap;
	}
}