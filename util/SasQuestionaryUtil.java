/**
 * 
 */
package com.sas.core.util;

import com.sas.core.constant.CommonConstant;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.meta.Sas;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 表单util
 * @author wuyiwen
 *
 */
public class SasQuestionaryUtil {
	
	protected static final Logger logger = Logger.getLogger(SasQuestionaryUtil.class);
	
	/********************
	 * 将表单字段解析成Map
	 * @return
	 */
	public static final Map<String,Object> convertQuestionaryFields2Map(final String qestionaryFileds)
	{
		if(StringUtils.isNotBlank(qestionaryFileds)){
			return JsonUtil.getObject(qestionaryFileds,Map.class);
		}else{
			return new HashMap<String,Object>();
		}
	}
	/********************
	 * 创建表单详情页的链接
	 * @param id
	 * @return
	 */
	public static final String createQuestionaryDetailPageURL(final Sas sas, final long questionaryId)
	{
		return "http://" + sas.getSubDomain() + "/admin/questionary/detail?questionaryId=" + questionaryId + "&mid=-8987";
	}
	
	/********************
	 * 创建表单列表页的链接
	 * @param id
	 * @return
	 */
	public static final String createQuestionaryListPageURL(final Sas sas)
	{
		return "http://" + sas.getSubDomain() + "/admin/questionary/";
	}
	
	/**
	 * json转List<BinaryEntry<String,String>>
	 * @param jsonString
	 * @return
	 */
	public static final List<BinaryEntry<String,String>> pasejsonToListEntry(final String jsonString)
	{
		List<BinaryEntry<String,String>> appliers = new ArrayList<BinaryEntry<String,String>>();
		//用户提交json转键值对
		if(jsonString != null){
			Map<String,Object> dataMap = JsonUtil.getObject(jsonString, Map.class);
			if(dataMap.size() > 1){
				for (Map.Entry<String,Object> entry : dataMap.entrySet()) {
					BinaryEntry<String, String> binaryEntry = new BinaryEntry<String, String>(entry.getKey(),entry.getValue().toString());
					appliers.add(binaryEntry);
				}
			}
		}
		return appliers;
	}
	
	/******************
	 * 参数代表表单字段，用&分隔，解析成
	 * @param fieldMap
	 * @return
	 */
	public static final Map<String,String> parseApplierFieldValues(final String fieldValues)
	{
		if(StringUtils.isBlank(fieldValues)){
			return null;
		}
		
		final String[] fieldNameValues = fieldValues.split(CommonConstant.DividerChar.Amp.chars);
		final HashMap<String,String> fieldsValuesMap = new HashMap<String,String>();
		for(final String fieldNameValue: fieldNameValues)
		{
			final int dividerCharIndex = fieldNameValue.indexOf(CommonConstant.DividerChar.Equal.chars);
			if(dividerCharIndex < 1){
				continue;
			}
			final String fieldName = fieldNameValue.substring(0, dividerCharIndex);
			final String fieldValue = (dividerCharIndex == (fieldNameValue.length()-1)) ? ""
					: HtmlUtil.decodeParam(fieldNameValue.substring(dividerCharIndex+1), CommonConstant.Encoding.UTF8, "");
			if(StringUtils.isBlank(fieldName) || StringUtils.isBlank(fieldValue)){
				continue;
			}
			String existValue = fieldsValuesMap.get(fieldName);
			if ( existValue == null){
				fieldsValuesMap.put(fieldName,fieldValue);
			} else{
				fieldsValuesMap.put(fieldName,fieldValue+CommonConstant.DividerChar.ComplexCharWithSemicolon.chars+existValue);
			}
		}
		return fieldsValuesMap;
	}
}