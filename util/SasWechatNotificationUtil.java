package com.sas.core.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.sas.core.constant.SasWechatNotificationConstant;
import com.sas.core.constant.TimeConstant.TimeFormat;

/**
 * 微信推送相关方法
 * 
 * @author zhangjun
 * 
 */
public class SasWechatNotificationUtil {

	private static final Logger logger = Logger.getLogger(SasWechatNotificationUtil.class);

//	// 调用微信接口获取access_token凭证
//	public static final String getToken(String apiurl, String appid, String secret) {
//		String turl = String.format("%s?grant_type=client_credential&appid=%s&secret=%s", apiurl,appid, secret);
//		final HttpClient client = new DefaultHttpClient();
//		final HttpGet get = new HttpGet(turl);
//		JsonParser jsonparer = new JsonParser();// 初始化解析json格式的对象
//		String result = null;
//		try {
//			final HttpResponse res = client.execute(get);
//			final HttpEntity entity = res.getEntity();
//			final String responseContent = EntityUtils.toString(entity, "UTF-8");
//			// 将json字符串转换为json对象
//			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//				JsonObject json = jsonparer.parse(responseContent).getAsJsonObject();
//				if (json.get("errcode") != null) {// 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid appid"}
//					logger.error("Fail to getToken, error="+ String.valueOf(json.get("errcode")));
//				} else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
//					result = json.get("access_token").getAsString();
//				}
//			}
//			logger.error("request new token:" + responseContent);
//		} catch (Exception e) {
//			logger.error("Fail to getToken, e="+e.getMessage(), e);
//		} finally {
//			// 关闭连接 ,释放资源
//			client.getConnectionManager().shutdown();
//			return result;
//		}
//	}

	// http请求
	public static String httpsRequest(String requestUrl, String requestMethod,String outputStr) {
		try {
			final URL url = new URL(requestUrl);
			final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod(requestMethod);
			conn.setRequestProperty("content-type","application/x-www-form-urlencoded");
			// 当outputStr不为null时向输出流写数据
			OutputStream outputStream = null;
			if (null != outputStr) {
				outputStream = conn.getOutputStream();
				// 注意编码格式
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}
			// 从输入流读取返回内容
			final InputStream inputStream = conn.getInputStream();
			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			final StringBuilder buffer = new StringBuilder();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			// 释放资源
			IOUtil.closeReaderWithoutException(bufferedReader);
			IOUtil.closeStreamWithoutException(inputStream, outputStream);
			IOUtil.closeStreamWithoutException(conn);
			return buffer.toString();
		}catch (Exception e) {
			logger.error("Fail to httpsRequest, e="+e.getMessage(), e);
			return null;
		}
	}
	
	
	/***
	 * {"action_name": "QR_LIMIT_SCENE", "action_info": {"scene": {"scene_id":
	 * 123}}} 或者也可以使用以下POST数据创建字符串形式的二维码参数： {"action_name":
	 * "QR_LIMIT_STR_SCENE", "action_info": {"scene": {"scene_str": "123"}}}
	 * 
	 * @param scene_id
	 *            场景值ID，临时二维码时为32位非0整型，永久二维码时最大值为100000（目前参数只支持1--100000）
	 * @param scene_str
	 *            场景值ID（字符串形式的ID），字符串类型，长度限制为1到64，仅永久二维码支持此字段
	 * @param action_name
	 *            二维码类型，QR_SCENE为临时,QR_LIMIT_SCENE为永久,
	 *            QR_LIMIT_STR_SCENE为永久的字符串参数值
	 * @return
	 */
	public static String getJson(String access_token, long scene_id ,String action_name, long expire_seconds) {
		final String url = SasWechatNotificationConstant.GET_TICKET_URL+access_token;
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> scene = new HashMap<String, Object>();
		final Map<String, Object> infoMap = new HashMap<String, Object>();// 二维码详细信息
		infoMap.put("scene_id", scene_id );
//		 infoMap.put("scene_str", scene_id);//也可以使用以下POST数据创建字符串形式的二维码参数：
		scene.put("scene", infoMap);
		map.put("expire_seconds", expire_seconds + "");
		map.put("action_name", action_name);
		map.put("action_info", scene);
		final JSONObject json = new JSONObject(map);
		String outputStr = json.toString();
		String result = SasWechatNotificationUtil.httpsRequest(url, "POST", outputStr);// 微信返回的信息
		logger.error("getJson=>result ： " +result);
		String error = "";
		final JSONObject jsonObject = new JSONObject(result);
		String rule = "\"errcode\":(\\d*)";
		final Pattern pattern = Pattern.compile(rule);
		final Matcher matcher = pattern.matcher(result);
		while (matcher.find()) {
			int gc = matcher.groupCount();
			error = matcher.group(gc);
		}
		if (StringUtils.isNotBlank(error)) {
			logger.error("getJson=>error ： " +error);
			result = null;
		} else {
			result = jsonObject.getString("ticket");
		}
		return result;
	}

	/**
	 * 排序方法
	 * 
	 * @param token
	 * @param timestamp
	 * @param nonce
	 * @return
	 */
	public static String sort(String token, String timestamp, String nonce) {
		String[] strArray = { token, timestamp, nonce };
		Arrays.sort(strArray);
		StringBuilder sbuilder = new StringBuilder("");
		for (String str : strArray) {
			sbuilder.append(str);
		}
		return sbuilder.toString();
	}

	/**
	 * sha1等加密工具
	 * @param decript
	 * @return
	 */
	public static String SHA1(String decript) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(decript.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
					}
				hexString.append(shaHex);
				}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Fail to sort, e="+e.getMessage(), e);
			}
		return "";
	}
	
	
	public static String generateWechatCode(){
		final String time = TimeUtil.formatCurrentTime(TimeFormat.YYYYMMDDHHMMSS);
		final String randStr = String.valueOf(RandomUtils.nextInt(999999) + 1000000).substring(1);
		return time + randStr;
	}
	
	public static void main(String[] args) {
		String token = "1Yf_Hy44tVnMdL6kFjsdxksiVds3CoL77vMvVhcm15vD_9zcKdP0CRd3brUo7yK2h-jhl5-LHThySA5ExAQEKv-DbYciqALoW4mRa2A2G4ETizqUmFd1oxgFUaGicBJ-HHBeAAAWWQ";
		final String ticket = getJson(token, 12, "QR_SCENE", 60*60);//获取微信生成的ticket
		System.out.println(ticket);
	}
}
	
