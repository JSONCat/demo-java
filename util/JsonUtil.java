package com.sas.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zhulm 13-6-6下午3:59
 */
public class JsonUtil {
	
	private static final Logger logger = Logger.getLogger(JsonUtil.class);

	/**
	 * jsonString转 对象
	 * 
	 * @param jsonString
	 *            json数据
	 * @param tClass
	 *            转义后的class
	 * @param <T>
	 *            class
	 * @return 列表
	 */
	public static <T> T getObject(String jsonString, Class<T> tClass) {
		if(StringUtils.isBlank(jsonString)){
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonString, tClass);
		} catch (Exception e) {
			logger.error("json format error " + jsonString, e);
		}
		return null;
	}

	/**
	 * jsonString转 对象
	 * 
	 * @param jsonString
	 *            json数据
	 * @param tClass
	 *            转义后的class
	 * @param features
	 *            转义参数
	 * @param <T>
	 *            class
	 * @return
	 */
	public static <T> T getObject(String jsonString, Class<T> tClass, DeserializationFeature... features) {
		ObjectMapper mapper = new ObjectMapper();
		for (DeserializationFeature feature : features) {
			mapper.disable(feature);
		}
		try {
			return mapper.readValue(jsonString, tClass);
		} catch (Exception e) {
			logger.error("json format error " + jsonString, e);
		}
		return null;
	}

	/**
	 * jsonString转 对象
	 * 
	 * @param jsonString
	 *            json数据
	 * @param tClass
	 *            转义后的class
	 * @param <T>
	 *            class
	 * @return 列表
	 */
	public static <T> T getObject(String jsonString, TypeReference<T> tClass) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonString, tClass);
		} catch (Exception e) {
			logger.error("json format error " + jsonString, e);
		}
		return null;
	}

	/**
	 * jsonString转 list
	 * 
	 * @param jsonString
	 *            json数据
	 * @param tClass
	 *            转义后的class
	 * @param <T>
	 *            class
	 * @return 列表
	 */
	public static <T> List<T> getList(String jsonString, final Class<T> tClass) {
		if(StringUtils.isBlank(jsonString)){
			return new ArrayList<T>(0);
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonString, new TypeReference<List<T>>() {
				@Override
				public Type getType() {
					return new ParameterizedType() {
						@Override
						public Type[] getActualTypeArguments() {
							return new Type[] { tClass };
						}

						@Override
						public Type getRawType() {
							return List.class;
						}

						@Override
						public Type getOwnerType() {
							return null;
						}
					};
				}
			});
		} catch (Exception e) {
			logger.error("json format error " + jsonString, e);
		}
		return null;
	}

	/**
	 * 对象转json
	 * 
	 * @param javaBean
	 *            对象
	 * @return json的字符串
	 */
	public static String getJsonString(Object javaBean) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(javaBean);
		} catch (JsonProcessingException e) {
			logger.error("json format error ", e);
		}
		return null;
	}
}
