/**
 * @Title: ThirdpartUtil.java
 * @Package com.sas.core.util
 * @author yunshang_734@163.com
 * @date Dec 20, 2014 4:14:49 PM
 * @version V1.0
 */
package com.sas.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.DomainConstant.SpecialDomain;
import com.sas.core.constant.ThirdPartConstant.MobileOrPC;
import com.sas.core.constant.ThirdPartConstant.PlatformSimpleType;
import com.sas.core.constant.ThirdPartConstant.PlatformType;
import com.sas.core.constant.ThirdPartConstant.QiNiuSpace;
import com.sas.core.constant.ThirdPartConstant.ThirdPartAPP;
import com.sas.core.constant.ThirdPartConstant.ThirdPartDomain;
import com.sas.core.constant.ThirdPartConstant.ThirdPartSource;
import com.sas.core.constant.UserConstant.SexType;
import com.sas.core.dto.ThirdPartStateParamDTO;
import com.sas.core.exception.AuthorityErrorException;
import com.sas.core.meta.PortalSite;
import com.sas.core.meta.Sas;
import com.sas.core.meta.ThirdpartQQ;
import com.sas.core.meta.ThirdpartWechat;
import com.sas.core.meta.ThirdpartWeibo;
import com.sas.core.meta.thirdpart.QQAccessToken;
import com.sas.core.meta.thirdpart.QQUser;
import com.sas.core.meta.thirdpart.WechatAccessToken;
import com.sas.core.meta.thirdpart.WechatUser;
import com.sas.core.meta.thirdpart.WeiboAccessToken;
import com.sas.core.meta.thirdpart.WeiboUser;

/**
 * @ClassName: ThirdpartUtil
 * @Description: 第三方登陆工具
 * @author yunshang_734@163.com
 * @date Dec 20, 2014 4:14:49 PM
 */
public class ThirdpartUtil {

	private final static Logger logger = Logger.getLogger(ThirdpartUtil.class);

	/****************
	 * 过滤掉赛会通的图片url
	 * 
	 * @param urls
	 * @return
	 */
	public static final List<String> removeSaihuitongImageURLs(final List<String> urls) {
		if (CollectionUtils.isEmpty(urls)) {
			return new ArrayList<String>(0);
		}
		final List<String> result = new LinkedList<String>();
		for (String url : urls) {
			if (ThirdpartUtil.isNotSaihuitongImageURL(url) && SpecialDomain.filterSubDomain(url) == null) {
				result.add(url);
			}
		}
		return result;
	}

	/******************
	 * 判读是否是其他网站的有效的url
	 * 
	 * @param url
	 * @return
	 */
	public static final boolean isNotSaihuitongImageURL(String url) {
		if (StringUtils.isBlank(url)) {
			return false;
		}
		url = url.trim().toLowerCase();
		// 是否有效路径， 我们发现有本地路径的
		if (!RequestUtil.isStartWithHttpOrHttps(url)) {
			return false;
		}
		// 如果没有域名，或者是我们自己的域名
		if (url.startsWith("/") || QiNiuSpace.isQiNiuImageURL(url)) {
			return false;
		}
		return true;
	}

	/**
	 * @Title: getAccessTokenUrl
	 * @Description: 获取AccessToken时请求的URL
	 * @param app
	 * @param code
	 * @return
	 * @throws
	 */
	public final static String getAccessTokenUrl(final ThirdPartAPP app, final String code) {
		if (app.thirdPartSource == ThirdPartSource.QQ) {
			final String redirect_uri = RequestUtil.encodeURLParam("http://www." + app.platformType.domain + "/thirdpart/qq/login",
					"http%3A%2F%2Fwww." + app.platformType.domain + "%2Fthirdpart%2Fqq%2Flogin");
			return ThirdPartDomain.QQAccessToken.domain + "?grant_type=authorization_code&client_id=" + app.appid + "&client_secret=" + app.appkey
					+ "&code=" + code + "&redirect_uri=" + redirect_uri;
		} else if (app.thirdPartSource == ThirdPartSource.Weibo) {
			final String redirect_uri = RequestUtil.encodeURLParam("http://www." + app.platformType.domain + "/thirdpart/weibo/login",  
					"http%3A%2F%2Fwww." + app.platformType.domain + "%2Fthirdpart%2Fweibo%2Flogin");
			return ThirdPartDomain.WeiboAccessToken.domain + "?grant_type=authorization_code&client_id=" + app.appid + "&client_secret=" + app.appkey
					+ "&code=" + code + "&redirect_uri=" + redirect_uri;
		} else if (app.thirdPartSource == ThirdPartSource.Wechat) {
			return ThirdPartDomain.WechatAccessToken.domain + "?grant_type=authorization_code&appid=" + app.appid + "&secret=" + app.appkey
					+ "&code=" + code;
		}
		return "";
	}

	/**
	 * @Title: getUserInfoUrl
	 * @Description: 获取用户信息URL
	 * @param app
	 * @param openId
	 * @param accessToken
	 * @return
	 * @throws
	 */
	public final static String getUserInfoUrl(final ThirdPartAPP app, final String openId, final String accessToken) {
		if (app.thirdPartSource == ThirdPartSource.QQ) {
			return ThirdPartDomain.QQUserInfo.domain + "?access_token=" + accessToken + "&oauth_consumer_key=" + app.appid + "&openid=" + openId
					+ "&format=json";
		} else if (app.thirdPartSource == ThirdPartSource.Weibo) {
			return ThirdPartDomain.WeiboUserInfo.domain + "?access_token=" + accessToken + "&uid=" + openId;
		} else if (app.thirdPartSource == ThirdPartSource.Wechat) {
			return ThirdPartDomain.WechatUserInfo.domain + "?access_token=" + accessToken + "&openid=" + openId;
		}
		return "";
	}

	/**
	 * @Title: getQQAccessToken
	 * @Description: QQAccessToken获取方法
	 * @param app
	 * @param code
	 * @param accessTokenUrl
	 * @return
	 * @throws
	 */
	public final static QQAccessToken getQQAccessToken(final ThirdPartAPP app, final String code, final String accessTokenUrl) {
		try {
			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Encoding.UTF8.type);
			GetMethod postMethod = new GetMethod(accessTokenUrl);
			if (httpClient.executeMethod(postMethod) != 200) {
				postMethod.abort();
				logger.error("Failed to executeMethod while getQQAccessToken!");
				return null;
			}
			final String returnData = postMethod.getResponseBodyAsString();
			final String[] paramArray = returnData.split("[&=]");
			if (!paramArray[0].equals("access_token")) {
				logger.error("Failed to getQQAccessToken because of " + returnData);
				postMethod.abort();
				return null;
			}
			final String accessToken = paramArray[1];
			final long expiredIn = IdUtil.convertTolong(paramArray[3], 0L);
			final String refreshToken = paramArray[5];
			QQAccessToken qqAccessToken = new QQAccessToken(accessToken, refreshToken, expiredIn);
			postMethod.abort();
			return qqAccessToken;
		} catch (IOException e) {
			throw new AuthorityErrorException(null, "Failed to getQQAccessToken with IOException : " + e);
		}
	}

	/**
	 * @Title: getQQOpenID
	 * @Description: 获取QQ用户OpenId
	 * @param accessToken
	 * @return
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public final static String getQQOpenID(final String accessToken) {
		try {
			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Encoding.UTF8.type);
			GetMethod postMethod = new GetMethod(ThirdPartDomain.QQUserOpenId.domain + "?access_token=" + accessToken);
			if (httpClient.executeMethod(postMethod) != 200) {
				postMethod.abort();
				logger.error("Failed to executeMethod while getQQOpenID!");
				return null;
			}
			String returnData = postMethod.getResponseBodyAsString();
			int beginIndex = returnData.indexOf("{");
			int endIndex = returnData.indexOf("}");
			Map<String, Object> resultMap = JsonUtil.getObject(returnData.substring(beginIndex - 1, endIndex + 1), Map.class);
			if (StringUtils.isBlank((String) resultMap.get("client_id"))) {
				logger.error("Failed to getQQOpenID because : " + returnData);
				postMethod.abort();
				return null;
			}
			postMethod.abort();
			return (String) resultMap.get("openid");
		} catch (IOException e) {
			throw new AuthorityErrorException(null, "Failed to getQQOpenID with IOException : " + e);
		}
	}

	/**
	 * @Title: getQQUserMap
	 * @Description: 获取QQ用户Map
	 * @param userInfoUrl
	 * @return
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public final static QQUser getQQUserMap(final String userInfoUrl) {
		try {
			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Encoding.UTF8.type);
			GetMethod postMethod = new GetMethod(userInfoUrl);
			if (httpClient.executeMethod(postMethod) != 200) {
				postMethod.abort();
				logger.error("Failed to executeMethod while getQQUserMap!");
				return null;
			}
			String returnData = postMethod.getResponseBodyAsString();
			final Map<String, Object> resultMap = JsonUtil.getObject(returnData, Map.class);
			if ((Integer) resultMap.get("ret") != 0) {
				logger.error("Failed to getQQUser because of code : " + resultMap.get("ret") + ", and error : " + resultMap.get("msg"));
				postMethod.abort();
				return null;
			}
			final QQUser qqUser = new QQUser(HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("nickname"), "")),
					HtmlUtil.defaultIfNull(resultMap.get("figureurl_qq_1"), ""), 
					HtmlUtil.defaultIfNull(resultMap.get("figureurl_qq_2"), ""),
					HtmlUtil.defaultIfNull(resultMap.get("gender"), ""),
					HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("province"), "")),
					HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("city"), "")));
			postMethod.abort();
			return qqUser;
		} catch (IOException e) {
			throw new AuthorityErrorException(null, "Failed to getQQUser with IOException : " + e);
		}
	}

	/**
	 * @Title: convertThirdpartQQ
	 * @Description: 将accessTokenMap和userInfoMap转换成微博用户信息
	 * @param accessTokenMap
	 * @param userInfoMap
	 * @return
	 * @throws
	 */
	public final static ThirdpartQQ convertThirdpartQQ(final QQAccessToken qqAccessToken, final String openId, final QQUser qqUser) {
		return new ThirdpartQQ(openId, qqAccessToken.getAccessToken(), 
				qqAccessToken.getRefreshToken(), qqUser.getNickname(),
				qqUser.getFigureurlQQ1(), qqUser.getFigureurlQQ2(), 
				"女".equals(qqUser.getGender()) ? SexType.Female : SexType.Male,
				qqUser.getProvince(), qqUser.getCity(), 
				System.currentTimeMillis(), System.currentTimeMillis());
	}

	/**
	 * @Title: getWeiboAccessToken
	 * @Description: 微博AccessToken获取方法
	 * @param app
	 * @param code
	 * @param accessTokenUrl
	 * @return
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public final static WeiboAccessToken getWeiboAccessToken(final ThirdPartAPP app, final String code, final String accessTokenUrl) {
		try {
			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Encoding.UTF8.type);
			PostMethod postMethod = new PostMethod(accessTokenUrl);
			NameValuePair[] data = { new NameValuePair("client_id", app.appid), new NameValuePair("client_secret", app.appkey),
					new NameValuePair("grant_type", "authorization_code"), new NameValuePair("code", code),
					new NameValuePair("redirect_uri", "http://www.saihuitong.com/thirdpart/weibo") };
			postMethod.setRequestBody(data);
			if (httpClient.executeMethod(postMethod) != 200) {
				postMethod.abort();
				logger.error("Failed to executeMethod while getWeiboAccessToken!");
				return null;
			}
			String returnData = postMethod.getResponseBodyAsString();
			Map<String, Object> resultMap = JsonUtil.getObject(returnData, Map.class);
			if (StringUtils.isNotBlank((String) resultMap.get("error_code"))) {
				logger.error("Failed to getWeiboAccessToken because : " + returnData);
				postMethod.abort();
				return null;
			}
			WeiboAccessToken weiboAccessToken = new WeiboAccessToken(
					HtmlUtil.defaultIfNull(resultMap.get("access_token"), ""),
					(Integer) resultMap.get("expires_in"),
					HtmlUtil.defaultIfNull(resultMap.get("remind_in"), ""),
					HtmlUtil.defaultIfNull(resultMap.get("uid"), ""));
			postMethod.abort();
			return weiboAccessToken;
		} catch (IOException e) {
			throw new AuthorityErrorException(null, "Failed to getWeiboAccessToken with IOException : " + e);
		}
	}

	/**
	 * @Title: getWeiboUserMap
	 * @Description: 获取微博用户Map
	 * @param userInfoUrl
	 * @return
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public final static WeiboUser getWeiboUserMap(final String userInfoUrl) {
		try {
			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Encoding.UTF8.type);
			GetMethod getMethod = new GetMethod(userInfoUrl);
			if (httpClient.executeMethod(getMethod) != 200) {
				getMethod.abort();
				logger.error("Failed to executeMethod while getWeiboUserMap!");
				return null;
			}
			final String returnData = getMethod.getResponseBodyAsString();
			Map<String, Object> resultMap = JsonUtil.getObject(returnData, Map.class);
			if (StringUtils.isNotBlank((String) resultMap.get("error_code"))) {
				logger.error("Failed to getWeiboUser because : " + returnData);
				getMethod.abort();
				return null;
			}
			final WeiboUser weiboUser = new WeiboUser(
					HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("screen_name"), "")),
					HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("province"), "")), 
					HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("city"), "")), 
					HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("location"), "")),
					HtmlUtil.defaultIfNull(resultMap.get("gender"), ""), 
					HtmlUtil.defaultIfNull(resultMap.get("profile_image_url"), ""), 
					HtmlUtil.defaultIfNull(resultMap.get("avatar_large"), ""),
					HtmlUtil.defaultIfNull(resultMap.get("avatar_hd"), ""));
			return weiboUser;
		} catch (IOException e) {
			throw new AuthorityErrorException(null, "Failed to getWeiboUser with IOException : " + e);
		}
	}

	/**
	 * @Title: convertThirdpartWeibo
	 * @Description: 将accessTokenMap和userInfoMap转换成微博用户信息
	 * @param accessTokenMap
	 * @param userInfoMap
	 * @return
	 * @throws
	 */
	public final static ThirdpartWeibo convertThirdpartWeibo(WeiboAccessToken weiboAccessToken, WeiboUser weiboUser) {
		return new ThirdpartWeibo(weiboAccessToken.getUid(), weiboAccessToken.getAccessToken(), weiboUser.getScreenName(),
				weiboUser.getProfileImageUrl(), weiboUser.getAvatarLarge(), weiboUser.getAvatarHD(),
				weiboUser.getGender().equals("f") ? SexType.Female : SexType.Male, weiboUser.getProvince(), weiboUser.getCity(),
				weiboUser.getLocation(), System.currentTimeMillis(), System.currentTimeMillis());
	}

	/**
	 * @Title: getWechatAccessToken
	 * @Description: 微信AccessToken获取方法
	 * @param app
	 * @param code
	 * @param accessTokenUrl
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public final static WechatAccessToken generateWechatAccessToken(final String accessTokenUrl) {
		try {
			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Encoding.UTF8.type);
			GetMethod postMethod = new GetMethod(accessTokenUrl);
			if (httpClient.executeMethod(postMethod) != 200) {
				postMethod.abort();
				logger.error("Failed to executeMethod while getWechatAccessToken!");
				return null;
			}
			String returnData = postMethod.getResponseBodyAsString();
			Map<String, Object> resultMap = JsonUtil.getObject(returnData, Map.class);
			if (StringUtils.isNotBlank((String) resultMap.get("errmsg"))) {
				logger.error("Failed to getWechatAccessToken because of code : " + resultMap.get("errmsg") + ", and error : "
						+ resultMap.get("errmsg"));
				postMethod.abort();
				return null;
			}
			postMethod.abort();
			return new WechatAccessToken(
					HtmlUtil.defaultIfNull(resultMap.get("access_token"), ""),
					(Integer) resultMap.get("expires_in"),
					HtmlUtil.defaultIfNull(resultMap.get("refresh_token"), ""),
					HtmlUtil.defaultIfNull(resultMap.get("openid"), ""),
					HtmlUtil.defaultIfNull(resultMap.get("scope"), ""),
					HtmlUtil.defaultIfNull(resultMap.get("unionid"), ""));
		} catch (IOException e) {
			logger.error("Failed to getWechatAccessToken with IOException : " + e);
			return null;
		}
	}

	/**
	 * @Title: getWechatUser
	 * @Description: 获取微信用户
	 * @param userInfoUrl
	 * @return
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public final static WechatUser getWechatUser(final String userInfoUrl) {
		try {
			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Encoding.UTF8.type);
			GetMethod postMethod = new GetMethod(userInfoUrl);
			if (httpClient.executeMethod(postMethod) != 200) {
				postMethod.abort();
				logger.error("Failed to executeMethod while getWechatUser!");
				return null;
			}
			String returnData = postMethod.getResponseBodyAsString();
			Map<String, Object> resultMap = JsonUtil.getObject(returnData, Map.class);
			if (StringUtils.isNotBlank((String) resultMap.get("error_code"))) {
				logger.error("Failed to getWechatUser because of code : " + resultMap.get("errcode") 
						+ ", and error : " + resultMap.get("errmsg"));
				postMethod.abort();
				return null;
			}
			final int sex = IdUtil.convertToInteger(String.valueOf(resultMap.get("sex")), 0);
			final WechatUser wechatUser = new WechatUser(
					HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("nickname"), "")),
					sex, 
					HtmlUtil.removeMobileUTF8MB4Emotions(HtmlUtil.defaultIfNull(resultMap.get("province"), "")), 
					HtmlUtil.removeMobileUTF8MB4Emotions(HtmlUtil.defaultIfNull(resultMap.get("city"), "")),
					HtmlUtil.removeMobileUTF8MB4Nickname(HtmlUtil.defaultIfNull(resultMap.get("country"), "")), 
					HtmlUtil.removeMobileUTF8MB4Emotions(HtmlUtil.defaultIfNull(resultMap.get("headimgurl"), "")),
					HtmlUtil.defaultIfNull(resultMap.get("unionid"), ""));
			return wechatUser;
		} catch (IOException e) {
			throw new AuthorityErrorException(null, "Failed to getWechatUser with IOException : " + e);
		}
	}

	/**
	 * 轉換成第三方微信用戶
	 * 
	 * @param accessTokenMap
	 * @param userInfoMap
	 * @return
	 */
	public final static ThirdpartWechat convertThirdpartWechat(WechatAccessToken wechatAccessToken, WechatUser wechatUser) {
		return new ThirdpartWechat(wechatAccessToken.getUnionId(), wechatUser.getUnionId(), wechatAccessToken.getAccessToken(),
				wechatAccessToken.getRefreshToken(), wechatUser.getNickname(), wechatUser.getHeadImgUrl(),
				(wechatUser.getSex() == 2 ? SexType.Female : SexType.Male), wechatUser.getCountry(), 
				wechatUser.getProvince(), wechatUser.getCity(), System.currentTimeMillis(),
				System.currentTimeMillis());
	}
	
	/*************
	 * 微信url
	 * @param sas
	 * @param platformType
	 * @param target
	 * @return
	 */
	public static final String createSasWeChatURL(final Sas sas, final PlatformType platformType, 
			final MobileOrPC mobileOrPC, String target, final long resaleShopUserId, final long userId){
		if(target != null){//微信会屏蔽&及其转义
			target = target.replaceAll("&", DividerChar.WeChatAndChars.chars);
		}
		final ThirdPartAPP wechatApp = ThirdPartAPP.parse(platformType, ThirdPartSource.Wechat);
		return "https://open.weixin.qq.com/connect/qrconnect?appid=" + wechatApp.appid + "&redirect_uri=http://www."
				+ wechatApp.platformType.domain + "/thirdpart/wechat/login" + "&response_type=code&scope=snsapi_login&state="
				+ new ThirdPartStateParamDTO(sas.getId(), target, PlatformSimpleType.Saihuitong, resaleShopUserId, userId).createStateParam()
				+ "#" + DividerChar.WeChatRedirectChars.chars;
	}

	public static final String createWapSasWeChatURL(final Sas sas, final PlatformType platformType, 
			final MobileOrPC mobileOrPC, String target, final long resaleShopUserId, final long userId){
		if(target != null){//微信会屏蔽&及其转义
			target = target.replaceAll("&", DividerChar.WeChatAndChars.chars);
		}
		final ThirdPartAPP wechatApp = ThirdPartAPP.parse(platformType, ThirdPartSource.Wechat);
		final String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + wechatApp.appid + "&redirect_uri=http%3A%2F%2Fwww."
			+ wechatApp.platformType.domain + "%2Fm%2Fthirdpart%2Fwechat%2Flogin&response_type=code&scope=snsapi_login&state=" 
			+ new ThirdPartStateParamDTO(sas.getId(), target, PlatformSimpleType.Saihuitong, resaleShopUserId, userId).createStateParam()
			+ "#" + DividerChar.WeChatRedirectChars.chars;
		return url;
	}

	public static final String createPortalWeChatURL(final PortalSite portalSite, final PlatformType platformType, 
			final MobileOrPC mobileOrPC, String target, final long resaleShopUserId, final long userId){
		if(target != null){//微信会屏蔽&及其转义
			target = target.replaceAll("&", DividerChar.WeChatAndChars.chars);
		}
		final ThirdPartAPP wechatApp = ThirdPartAPP.parse(platformType, ThirdPartSource.Wechat);
		return "https://open.weixin.qq.com/connect/qrconnect?appid=" + wechatApp.appid
				+ "&redirect_uri=http://www.saihuitong.com/thirdpart/wechat/login" + "&response_type=code&scope=snsapi_login&state="
				+ new ThirdPartStateParamDTO(portalSite.getId(), target, PlatformSimpleType.BieZhaiLa, resaleShopUserId, userId).createStateParam()
				+ "#" + DividerChar.WeChatRedirectChars.chars;
	}	

	public static final String createWapPortalWeChatURL(final PortalSite portalSite, final PlatformType platformType, 
			final MobileOrPC mobileOrPC, String target, final long resaleShopUserId, final long userId){
		if(target != null){//微信会屏蔽&及其转义
			target = target.replaceAll("&", DividerChar.WeChatAndChars.chars);
		}
		final ThirdPartAPP wechatApp = ThirdPartAPP.parse(platformType, ThirdPartSource.Wechat);
		return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + wechatApp.appid
				+ "&redirect_uri=http://www.saihuitong.com/thirdpart/wechat/login" + "&response_type=code&scope=snsapi_login&state="
				+ new ThirdPartStateParamDTO(portalSite.getId(), target, PlatformSimpleType.BieZhaiLa, resaleShopUserId, userId).createStateParam()
				+ "#" + DividerChar.WeChatRedirectChars.chars;
	}	
	
	
	/***********
	 * QQ URL
	 * @param sas
	 * @param platformType
	 * @param target
	 * @return
	 */
	public static final String createSasQQURL(final Sas sas, final PlatformType platformType, final MobileOrPC mobileOrPC,
			final String target, final long resaleShopUserId, final long userId){
		ThirdPartAPP qqApp = ThirdPartAPP.parse(platformType, ThirdPartSource.QQ);
		return "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=" + qqApp.appid + "&redirect_uri=http://www."
					+ qqApp.platformType.domain + "/thirdpart/qq/login&state=" 
					+ new ThirdPartStateParamDTO(sas.getId(), target, PlatformSimpleType.Saihuitong, resaleShopUserId, userId).createStateParam() ;
	}

	public static final String createPortalQQURL(final PortalSite portalSite, final PlatformType platformType, 
			final MobileOrPC mobileOrPC, final String target, final long resaleShopUserId, final long userId){
		ThirdPartAPP qqApp = ThirdPartAPP.parse(platformType, ThirdPartSource.QQ);
		return "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=" + qqApp.appid
					+ "&redirect_uri=http://www.saihuitong.com/thirdpart/qq/login&state=" 
					+ new ThirdPartStateParamDTO(portalSite.getId(), target, PlatformSimpleType.BieZhaiLa, resaleShopUserId, userId).createStateParam();
	}
	
	/***********
	 * 微博URL
	 * @param sas
	 * @param platformType
	 * @param target
	 * @return
	 */
	public static final String createSasWeiboURL(final Sas sas, final PlatformType platformType,
			final MobileOrPC mobileOrPC, final String target, final long resaleShopUserId, final long userId){
		ThirdPartAPP weiboApp = ThirdPartAPP.parse(platformType, ThirdPartSource.Weibo);
		return "https://api.weibo.com/oauth2/authorize?client_id=" + weiboApp.appid + "&redirect_uri=http://www."
					+ weiboApp.platformType.domain + "/thirdpart/weibo/login&state=" 
					+ new ThirdPartStateParamDTO(sas.getId(), target, PlatformSimpleType.Saihuitong, resaleShopUserId, userId).createStateParam() 
					+ "&scope=all&display=default&forcelogin=false";
	}
	
	public static final String createPortalWeiboURL(final PortalSite portalSite, final PlatformType platformType,
			final MobileOrPC mobileOrPC, final String target, final long resaleShopUserId, final long userId){
		ThirdPartAPP weiboApp = ThirdPartAPP.parse(platformType, ThirdPartSource.Weibo);
		return "https://api.weibo.com/oauth2/authorize?client_id=" + weiboApp.appid
					+ "&redirect_uri=http://www.saihuitong.com/thirdpart/weibo/login&state="
					+ new ThirdPartStateParamDTO(portalSite.getId(), target, PlatformSimpleType.BieZhaiLa, resaleShopUserId, userId).createStateParam() 
					+ "&scope=all&display=default&forcelogin=false";
	}
	


}