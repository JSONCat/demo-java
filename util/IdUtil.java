package com.sas.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author zhulm 13-6-4下午1:00
 */
public class IdUtil {
	
	private static final Logger logger = Logger.getLogger(IdUtil.class);
	
	/**
	 * long数组转成Long数组
	 * 
	 * @param ids  long数组
	 * @return Long[]数组
	 */
	public static Long[] convertIdsLong(long[] ids) {
		if(ArrayUtils.isEmpty(ids)){
			return new Long[0];
		}
		return ArrayUtils.toObject(ids);
	}
	
	/**
	 * Long数组转成long数组
	 * 
	 * @param ids
	 *            Long[]数组
	 * @return long数组
	 */
	public static long[] convertIdslong(Long[] ids) {
		if(ids == null){
			return null;
		}
		return ArrayUtils.toPrimitive(ids);
	}
	
	
	/**
	 * long 列表 转成Long数组
	 * @param ids long列表
	 * @return long 数组
	 */
	public static Long[] convertIdslongNative(Collection<Long> ids) {
		if(ids == null || ids.size() == 0){
			return null;
		}
		
		Long[] lids = new Long[ids.size()];
		int index = 0;
		for (Long id : ids) {
			lids[index++] = id;
		}
		return lids;
	}

	/**
	 * String数组转成long数组，空串不返回
	 * 
	 * @param ids
	 * @return
	 */
	public static Long[] convertIdslong(String[] ids) {
		if(ids == null){
			return null;
		}
		List<Long> arr = new ArrayList<Long>();
		for (String id : ids) {
			if (StringUtils.isNotBlank(id))
				arr.add(Long.parseLong(id));
		}
		return arr.toArray(new Long[arr.size()]);
	}
	
	/**
	 * String数组转成Long数组，空串不返回
	 * 
	 * @param ids
	 * @return
	 */
	public static Long[] convertIdsLong(String[] ids) {
		if(ids == null){
			return null;
		}
		
		List<Long> arr = new ArrayList<Long>();
		for (String id : ids) {
			if (StringUtils.isNotBlank(id))
				arr.add(Long.parseLong(id));
		}
		return arr.toArray(new Long[arr.size()]);
	}
	
	/**
	 * String数组转成Long数组，空串不返回
	 * 
	 * @param ids
	 * @return
	 */
	public static long convertTolong(String id, long defaultV) {
		if (StringUtils.isNotBlank(id)){
			try{
				return Long.valueOf(id.replaceAll("(#)|(\\t)|(\\r)|(\\n)|(&nbsp;)|( )|(　)|(&lt;)|(&gt;)", ""));
			}catch(Exception ex){
				logger.error("fail to convertIdlong:" + id, new Exception());
				return defaultV;
			}
		}
		return defaultV;
	}
	
	public static long convertTolong(String id, final int radix, long defaultV) {
		if (StringUtils.isNotBlank(id)){
			try{
				return Long.valueOf(id.replaceAll("(#)|(\\t)|(\\r)|(\\n)|(&nbsp;)|( )|(　)|(&lt;)|(&gt;)", ""), radix);
			}catch(Exception ex){
				logger.error("fail to convertTolong:" + id, new Exception());
				return defaultV;
			}
		}
		return defaultV;
	}
	
	public static long convertTolong(Object id, long defaultV) {
		if (id != null){
			try{
				return Long.valueOf(String.valueOf(id).replaceAll("(#)|(\\t)|(\\r)|(\\n)|(&nbsp;)|( )|(　)|(&lt;)|(&gt;)", ""));
			}catch(Exception ex){
				logger.error("fail to convertIdlong:" + id, new Exception());
				return defaultV;
			}
		}
		return defaultV;
	}
	
	public static long convertTolongByReTryFilter(String id, long defaultV) {
		if (StringUtils.isNotBlank(id)){
			try{
				return Long.valueOf(id.replaceAll("(#)|(\\t)|(\\r)|(\\n)|(&nbsp;)|( )|(　)|(&lt;)|(&gt;)", ""));
			}catch(Exception ex){
				return IdUtil.convertTolong(IdUtil.fetchNumber(id, null), defaultV);
			}
		}
		return defaultV;
	}
	
	/***********
	 * 仅仅获取数字
	 * @param id
	 * @param defaultValue
	 * @return
	 */
	public static String fetchNumber(String id, final String defaultValue) {
		if (StringUtils.isNotBlank(id)){
			final StringBuilder sb = new StringBuilder("");
			for(int i=0; i<id.length(); i++){
				final char ch = id.charAt(i);
				if(ch >= '0' && ch <= '9'){
					sb.append(ch);
				}
			}
			return sb.toString();
		}
		return defaultValue;
	}

	public static int convertToInteger(String v, int defaultV) {
		if (StringUtils.isNotBlank(v)){
			try{
				return Integer.valueOf(v.replaceAll("(#)|(\\t)|(\\r)|(\\n)|(&nbsp;)|( )|(　)|(&lt;)|(&gt;)", ""));
			}catch(Exception ex){
				logger.error("fail to convert2Integer:" + v, new Exception());
				return defaultV;
			}
		}
		return defaultV;
	}

	public static int convertToNumber(String v, int defaultV) {
		if (StringUtils.isBlank(v)){
			return defaultV;
		}
		int result = 0;
		for(int i=0; i<v.length(); i++){
			char ch = v.charAt(i);
			if(ch >= '0' && ch <= '9'){
				result = result * 10 + (ch - '0');
			}
		}
		return result;
	}
	
	public static double convertToDouble(String v, double defaultV) {
		if (StringUtils.isNotBlank(v)){
			try{
				return Double.valueOf(v.replaceAll("(#)|(\\t)|(\\r)|(\\n)|(&nbsp;)|( )|(　)|(&lt;)|(&gt;)", ""));
			}catch(Exception ex){
				logger.error("fail to convertToDouble:" + v, new Exception());
				return defaultV;
			}
		}
		return defaultV;
	}
	
	public static void main(String[] args){
		System.out.println(IdUtil.convertTolong("2634#", 1));
	}
	
	/**
	 * String 列表 转成Long数组，空串不返回
	 * 
	 * @param ids
	 * @return
	 */
	public static Long[] convertIdsLong(List<String> ids) {
		List<Long> arr = new ArrayList<Long>();
		for (String id : ids) {
			if (StringUtils.isNotBlank(id))
				arr.add(Long.parseLong(id));
		}
		return arr.toArray(new Long[arr.size()]);
	}

	public static long[] convertIdslong(List<Long> ids){
		final long[] list = new long[ids.size()];
		int index = -1;
		for(Long id: ids){
			list[++index] = id;
		}
		return list;
	}
	
	/**
	 * 分隔id数据
	 * 
	 * @param ids
	 * @param startIdx
	 *            开始位置， 从0开始
	 * @param endIdx
	 *            结束位置（包含，要返回）， 最大值为 ids.length -1
	 * @return
	 */
	public static long[] splitIds(long[] ids, int startIdx, int endIdx) {
		int len = endIdx - startIdx + 1;
		final int totalLen = ids.length;
		final int newLen = totalLen - startIdx;
		if (newLen < len) {
			len = newLen;
		}
		long[] retIds = new long[len];
		System.arraycopy(ids, startIdx, retIds, 0, len);
		return retIds;
	}

	/**
	 * 根据分隔符解析id串，允许末尾字符是分隔符
	 * 
	 * @param idsString
	 *            以splitChar分割的字符串
	 * @param separator
	 *            分割符
	 * @return 异常时返回null，其他情况返回链表
	 */
	public static List<Long> parseIdsBySplit(final String idsString, final String separator) {		
		if (StringUtils.isBlank(idsString)){
			return new ArrayList<Long>(0);
		}
		final String[] idStrs = idsString.split(separator);
		final List<Long> idsList = new ArrayList<Long>(idStrs.length);
		for (final String idStr : idStrs) {
			if (StringUtils.isNotBlank(idStr))
				idsList.add(IdUtil.convertTolong(StringUtils.trim(idStr), 0));
		}
		return idsList;
	}

	public static Long[] parseIdsBySplit2Array(final String idsString, final String separator) {		
		if (StringUtils.isBlank(idsString)){
			return new Long[0];
		}
		final String[] idStrs = idsString.split(separator);
		final Long[] idsList = new Long[idStrs.length];
		for (int index=0; index<idStrs.length;  index++) {
			idsList[index] = IdUtil.convertTolong(StringUtils.trim(idStrs[index]), 0);
		}
		return idsList;
	}
	
	public static Set<Long> parseIdsBySplit2Set(final String idsString, final String separator) {		
		if (StringUtils.isBlank(idsString)){
			return new HashSet<Long>(0);
		}
		final String[] idStrs = idsString.split(separator);
		final Set<Long> idSet = new HashSet<Long>();
		for (final String idStr : idStrs) {
			if (StringUtils.isNotBlank(idStr))
				idSet.add(IdUtil.convertTolong(StringUtils.trim(idStr), 0));
		}
		return idSet;
	}
	
	/**
	 * 根据分隔符拼装id串
	 * 
	 * @param idsList
	 *            id链表
	 * @param separator
	 *            分割符
	 * @return 异常时返回null，其他情况返回以splitChar分割的字符串
	 */
	public static String joinIdsToString(List<Long> idsList, String separator) {
		return StringUtils.join(idsList, separator);
	}

	/**
	 * 向原有ID串前面添加ID数据
	 * 
	 * @param sourceIdsString
	 *            原有ID串
	 * @param appendIds
	 *            前面添加的ID数据
	 * @param isAppendToTail
	 *            true添加到尾部，false添加到头部。
	 * @return
	 */
	public static String appendIdsToSourceIdsStringBySplit(String sourceIdsString, final Long[] appendIds, final String separator, boolean isAppendToTail) {
		final StringBuilder result = new StringBuilder("");
		final int sourceIdStringLength = (sourceIdsString == null) ? 0 : sourceIdsString.length();
		if (!isAppendToTail) {
			// 添加到头部
			for (int i=0; i<appendIds.length; i++) {
				result.append(appendIds[i]);
				if(i < (appendIds.length-1) || sourceIdStringLength > 0){
					result.append(separator);
				}
			}
			if(sourceIdStringLength > 0){
				result.append(sourceIdsString);
			}
		} else {
			// 添加到尾部
			if(sourceIdStringLength > 0){
				result.append(sourceIdsString);
			}
			for (int i=0; i<appendIds.length; i++) {
				if(i > 0 || sourceIdStringLength > 0){
					result.append(separator);
				}
				result.append(appendIds[i]);
			}
		}
		return result.toString();
	}

	/**
	 * 从原有ID串中间剔除ID数据，类似removeIds接口
	 * 
	 * @param sourceIdsString
	 *            以splitChar分割的字符串
	 * @param removedIds
	 *            要剔除的ID
	 * @param separator
	 *            分割符
	 * @return
	 */
	public static String removeIdsFromSourceIdsStringBySplit(final String sourceIdsString, final Long[] removedIds, final String separator) {
		if(StringUtils.isBlank(sourceIdsString)){
			return "";
		}
		final String[] s = sourceIdsString.split(separator);
		final Set<String> sets = new HashSet<String>();
		Collections.addAll(sets, s);
		for (long id : removedIds) {
			sets.remove(String.valueOf(id));
		}
		final StringBuilder sb = new StringBuilder("");
		for (String ss : s) {
			if (sets.contains(ss)) {
				sb.append(ss);
				sb.append(separator);
			}
		}
		return sb.toString();
	}
}
