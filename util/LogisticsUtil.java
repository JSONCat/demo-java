package com.sas.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sas.core.dto.BinaryEntry;

/**
 * 商品物流信息
 * 
 * @author zhangjun
 * 
 */
public class LogisticsUtil {
	
	private static final Logger logger=Logger.getLogger(LogisticsUtil.class);

	// http://api.kuaidi.com/openapi.html?id=df48046372a14836f5adf3ce571da665&com=yunda&nu=3903743124103&show=0
	// 快递查询url
	private static final String URL = "http://api.kuaidi.com/openapi.html";

	// 快递查询授权key
	private static final String key = "df48046372a14836f5adf3ce571da665";

	/**
	 * 获取物流信息json数据
	 * 
	 * @param com
	 *            快递公司代号
	 * @param nu
	 *            快递号
	 * @return
	 */
	public static String searchKuaidi(String com, String nu) {
		final StringBuilder content = new StringBuilder("");

		try {
			final URL url = new URL(URL + "?id=" + key + "&com=" + com + "&nu=" + nu);
			final URLConnection connection = url.openConnection();
			connection.setAllowUserInteraction(false);
			final InputStream inputStream = url.openStream();
			final byte b[] = new byte[16392];
			int numRead = inputStream.read(b);
			content.append(new String(b, 0, numRead));
			while (numRead != -1) {
				numRead = inputStream.read(b);
				if (numRead != -1) {
					// String newContent = new String(b, 0, numRead);
					content.append(new String(b, 0, numRead, "UTF-8"));
				}
			}
			inputStream.close();
		} catch (MalformedURLException ex) {
			logger.error("fail to connect URl :url="+URL+",ex="+ex.getMessage(),ex);			
		} catch (IOException ex) {
			logger.error("Failed to convert InputStream to string with IOException : " + ex, ex);
		}
		return content.toString();
	}

	/**
	 * 提取物流信息
	 * 
	 * @param com
	 *            物流公司代号
	 * @param nu
	 *            物流号
	 * @return
	 */
	public static final BinaryEntry<Boolean, List<String>> getLogisticsProgressInformation(final String com,
			final String nu)
	{
		final List<String> contexts = new LinkedList<String>();
		boolean isDeliverySuccessFinish = false;
		BinaryEntry<Boolean, List<String>> result = new BinaryEntry<Boolean, List<String>>(
				isDeliverySuccessFinish, contexts);
		final String content = LogisticsUtil.searchKuaidi(com, nu);
		if (StringUtils.isBlank(content)) {
			result = null;
		} else {
			JSONObject jsonObject = new JSONObject(content);
			boolean success = jsonObject.getBoolean("success");
			if (success) {
				JSONArray array = jsonObject.getJSONArray("data");
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					String time = object.getString("time");
					String context = object.getString("context");
					contexts.add(time + " " + context);
				}
				Integer state = jsonObject.getInt("status");
				if (state == 6) {
					isDeliverySuccessFinish = true;
				}
				result.key = isDeliverySuccessFinish;
				result.value = contexts;
			} else {
				result = null;
			}
		}
		return result;
	}

	public static void main(String[] args) {
		// Map<String, Object> map = showContent("yunda", "3903743124103");
		BinaryEntry<Boolean, List<String>> entry = getLogisticsProgressInformation("yunda",
				"3903743124103");
		System.out.println(entry);
	}

}
