package com.sas.core.util.LGJ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.CommonConstant.PageLimit;
import com.sas.core.constant.LGJConstant;
import com.sas.core.dto.PageData;
import com.sas.core.dto.Paginator;
import com.sas.core.meta.LGJ.LGJTask;
import com.sas.core.meta.LGJ.LGJUser;
import com.sas.core.meta.LGJ.LGJUserDetail;
import com.sas.core.meta.LGJ.LGJUserExt;
import com.sas.core.util.HtmlUtil;
import com.sas.core.util.IdUtil;
import com.sas.core.util.JsonUtil;

/**
 * @Description: 
 * @Date: Jun 27, 2015
 * @Time: 3:54:43 PM
 */
public class TaskUserUtil {
	
	private static final Logger logger = Logger.getLogger(TaskUserUtil.class);
	
	/**
	 * @Description: 将用户Map转换成对象
	 * @Date: Jun 27, 2015
	 * @Time: 3:52:48 PM
	 * @param lgjTask
	 * @param userMap
	 * @return
	 */
	public static final LGJUser convertLGJUserMapToLGJUser(final LGJTask lgjTask, final Map<String, Object> userMap) {
		return new LGJUser(lgjTask.getId(),
				IdUtil.convertTolong(userMap.get(LGJConstant.UserId), 0),
				IdUtil.convertTolong(userMap.get(LGJConstant.ClubId), 0),
				userMap.get(LGJConstant.Username) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) userMap.get(LGJConstant.Username)),
				userMap.get(LGJConstant.Phone) == null ? "" : (String) userMap.get(LGJConstant.Phone),
				userMap.get(LGJConstant.UserAvatar) == null ? "" : (String) userMap.get(LGJConstant.UserAvatar),
				userMap.get(LGJConstant.Password) == null ? "" : (String) userMap.get(LGJConstant.Password),
				userMap.get(LGJConstant.Status) == null ? 0 : ((Number) userMap.get(LGJConstant.Status)).intValue(),
				userMap.get(LGJConstant.UserType) == null ? 0 : ((Number) userMap.get(LGJConstant.UserType)).intValue(),
				userMap.get(LGJConstant.Nickname) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) userMap.get(LGJConstant.Nickname)),
				userMap.get(LGJConstant.CreateTime) == null ? 0L : ((Number) userMap.get(LGJConstant.CreateTime)).longValue());
	}

	/**
	 * @Description: 将用户附加信息Map转换成对象
	 * @Date: Jun 27, 2015
	 * @Time: 3:52:28 PM
	 * @param lgjTask
	 * @param userExtMap
	 * @return
	 */
	public static final LGJUserExt convertLGJUserExtMapToLGJUserExt(final LGJTask lgjTask, final Map<String, Object> userExtMap) {
		if(userExtMap == null){
			return new LGJUserExt();
		}
		return new LGJUserExt(lgjTask.getId(),
				IdUtil.convertTolong(userExtMap.get(LGJConstant.UserId), 0),
				userExtMap.get(LGJConstant.Age) == null ? 0 : ((Number) userExtMap.get(LGJConstant.Age)).intValue(),
				userExtMap.get(LGJConstant.Birthday) == null ? 0L : ((Number) userExtMap.get(LGJConstant.Birthday)).longValue(),
				userExtMap.get(LGJConstant.City) == null ? "" : (String) userExtMap.get(LGJConstant.City),
				userExtMap.get(LGJConstant.EmergencyName) == null ? "" : (String) userExtMap.get(LGJConstant.EmergencyName),
				userExtMap.get(LGJConstant.EmergencyPhone) == null ? "" : (String) userExtMap.get(LGJConstant.EmergencyPhone),
				userExtMap.get(LGJConstant.Hobby) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) userExtMap.get(LGJConstant.Hobby)),
				userExtMap.get(LGJConstant.IDCard) == null ? "" : (String) userExtMap.get(LGJConstant.IDCard),
				userExtMap.get(LGJConstant.LastLoginDeviceId) == null ? "" : (String) userExtMap.get(LGJConstant.LastLoginDeviceId),
				userExtMap.get(LGJConstant.LastLoginIp) == null ? "" : (String) userExtMap.get(LGJConstant.LastLoginIp),
				userExtMap.get(LGJConstant.LastLoginPlatform) == null ? "" : (String) userExtMap.get(LGJConstant.LastLoginPlatform),
				userExtMap.get(LGJConstant.LastLoginTime) == null ? 0L : ((Number) userExtMap.get(LGJConstant.LastLoginTime)).longValue(),
				userExtMap.get(LGJConstant.Name) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) userExtMap.get(LGJConstant.Name)),
				userExtMap.get(LGJConstant.Promoter) == null ? 0 : ((Number) userExtMap.get(LGJConstant.Promoter)).intValue(),
				userExtMap.get(LGJConstant.Province) == null ? "" : (String) userExtMap.get(LGJConstant.Province),
				userExtMap.get(LGJConstant.Referer) == null ? 0 : ((Number) userExtMap.get(LGJConstant.Referer)).intValue(),
				userExtMap.get(LGJConstant.RefererSite) == null ? 0 : ((Number) userExtMap.get(LGJConstant.RefererSite)).intValue(),
				userExtMap.get(LGJConstant.RegisterClientIp) == null ? "" : (String) userExtMap.get(LGJConstant.RegisterClientIp),
				userExtMap.get(LGJConstant.Sex) == null ? 0 : ((Number) userExtMap.get(LGJConstant.Sex)).intValue(),
				userExtMap.get(LGJConstant.Signature) == null ? "" : (String) userExtMap.get(LGJConstant.Signature),
				userExtMap.get(LGJConstant.UpdateTime) == null ? 0L : ((Number) userExtMap.get(LGJConstant.UpdateTime)).longValue());
	}
	
	/**
	 * @Description: 获取用户详情列表
	 * @Date: Jun 27, 2015
	 * @Time: 3:52:13 PM
	 * @param sourceDomain
	 * @param lgjTask
	 * @param page
	 * @param time
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static final PageData<LGJUserDetail> getLGJUserDetailList(final LGJTask lgjTask,
			final int page) throws HttpException, IOException {
		final HttpClient httpClient = new HttpClient();
		final String url = TaskBaseUtil.getUserListUrl(lgjTask.getSourceDomain(), page);
		final GetMethod getMethod = new GetMethod(url);   
		TaskBaseUtil.setRequestHeaderAndCookies(getMethod, null, lgjTask);		
		if (httpClient.executeMethod(getMethod) != 200) {
			getMethod.abort();
			logger.error("Failed to executeMethod with url : " + url + ", StatusLine : " + getMethod.getStatusLine());
			return null;
		} else {
			final String returnData = getMethod.getResponseBodyAsString();
			final Map<String, Object> returnMap = JsonUtil.getObject(returnData, Map.class);
			final Integer code = (Integer) returnMap.get(LGJConstant.Code);
			if (code == null || !code.equals(200)) {
				logger.error("Failed to getLGJUserDetailList with sourceDomain : " + lgjTask.getSourceDomain() + " & page : " + page);
				return null;
			}
			final Map<String, Object> resultMap = (Map<String, Object>) returnMap.get(LGJConstant.Result);
			final List<Map<String, Object>> list = (List<Map<String, Object>>) resultMap.get(LGJConstant.List);
			List<LGJUserDetail> detailList = new ArrayList<LGJUserDetail>(list.size());
			for (Map<String, Object> map : list) {
				Map<String, Object> userMap = (Map<String, Object>) map.get(LGJConstant.User);
				Map<String, Object> userExtMap = (Map<String, Object>) map.get(LGJConstant.UserExt);
				detailList.add(new LGJUserDetail(convertLGJUserMapToLGJUser(lgjTask, userMap),
						convertLGJUserExtMapToLGJUserExt(lgjTask, userExtMap)));
			}
			getMethod.abort();
			final Paginator paginator = new Paginator(PageLimit.Ten.limit, (Integer) resultMap.get(LGJConstant.Total));
			return new PageData<LGJUserDetail>(detailList, paginator);
		}
	}
}