package com.sas.core.util.LGJ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.sas.core.constant.LGJConstant;
import com.sas.core.dto.PageData;
import com.sas.core.meta.LGJ.LGJTask;
import com.sas.core.meta.LGJ.LGJUserDetail;
import com.sas.core.util.JsonUtil;

/**
 * @Description: 迁移任务基本工具类
 * @Date: Jun 26, 2015
 * @Time: 4:19:15 PM
 */
public class TaskBaseUtil {
	
	private static final Logger logger = Logger.getLogger(TaskBaseUtil.class);
	
	/************
	 * 设置cookie
	 * @param httpClient
	 * @param getMethod
	 * @param postMethod
	 * @param lgjTask
	 */
	public static final void setRequestHeaderAndCookies(final GetMethod getMethod,
			final PostMethod postMethod, final LGJTask lgjTask)
	{
		if(getMethod != null){
			//getMethod.addRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01");    
			//getMethod.addRequestHeader("Accept-Encoding", "gzip, deflate");    
			//getMethod.addRequestHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");    
			getMethod.addRequestHeader("Connection", "keep-alive");          
			//getMethod.addRequestHeader("Upgrade-Insecure-Requests", "1");        
			getMethod.addRequestHeader("Host", lgjTask.getSourceDomain());    
			getMethod.addRequestHeader("Refer", lgjTask.getSourceDomain());
			getMethod.addRequestHeader("Cookie",  lgjTask.getUserIdCookie()); 
			//getMethod.addRequestHeader("Content-Type",  "text/html; charset=UTF-8"); 
			//getMethod.getParams().setContentCharset("UTF-8");
		}
		if(postMethod != null){
			//postMethod.addRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01"); 
			//postMethod.addRequestHeader("Accept-Encoding", "gzip, deflate");    
			//postMethod.addRequestHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");    
			postMethod.addRequestHeader("Connection", "keep-alive");          
			//postMethod.addRequestHeader("Upgrade-Insecure-Requests", "1");        
			postMethod.addRequestHeader("Host", lgjTask.getSourceDomain());    
			postMethod.addRequestHeader("Refer", lgjTask.getSourceDomain());
			postMethod.addRequestHeader("Cookie",  lgjTask.getUserIdCookie()); 
			//postMethod.addRequestHeader("Content-Type",  "text/html; charset=UTF-8"); 
			//postMethod.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
		}
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 3, 2015
	 * @Time: 11:06:58 AM
	 * @param sourceDomain
	 * @param username
	 * @param password
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean testUsernameAndPassword(final LGJTask lgjTask) {
		final HttpClient httpClient = new HttpClient();
		final String url = TaskBaseUtil.getLoginUrl(lgjTask.getUsername(), lgjTask.getPassword());
		PostMethod postMethod = new PostMethod(url);
		try {
			if (httpClient.executeMethod(postMethod) != 200) {
				postMethod.abort();
				logger.error("Failed to executeMethod with url : " + url + ", StatusLine : " + postMethod.getStatusLine());
				return false;
			} else {
				final String returnData = postMethod.getResponseBodyAsString();
				final Map<String, Object> returnMap = JsonUtil.getObject(returnData, Map.class);
				final Integer code = (Integer) returnMap.get(LGJConstant.Code);
				if (code == null || !code.equals(200)) {
					logger.error("Failed to testUsernameAndPassword with sourceDomain : " + lgjTask.getSourceDomain() + ", username : " + lgjTask.getUsername() + ", password : " + lgjTask.getPassword());
					return false;
				} else {
					System.out.println(returnMap.toString());
					return true;
				}
			}
		} catch (HttpException e) {
			logger.error("Failed to testUsernameAndPassword with HttpException : " + e, e);
			return false;
		} catch (IOException e) {
			logger.error("Failed to testUsernameAndPassword with IOException : " + e, e);
			return false;
		}
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 3, 2015
	 * @Time: 11:07:01 AM
	 * @param sourceDomain
	 * @param userIdCookie
	 * @param userSignCookie
	 * @param createDataCookie
	 * @return
	 */
	public static boolean testCookies(final LGJTask lgjTask) {
		try {
			final PageData<LGJUserDetail> pageData = TaskUserUtil.getLGJUserDetailList(lgjTask, 1);
			final List<LGJUserDetail> detailList = pageData == null ? new ArrayList<LGJUserDetail>(0) : pageData.getElements();
			if (detailList.size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (HttpException e) {
			logger.error("Failed to testCookies with HttpException : " + e, e);
			return false;
		} catch (IOException e) {
			logger.error("Failed to testCookies with IOException : " + e, e);
			return false;
		}
	}
	
	/**
	 * @Description: 获取登陆URL
	 * @Date: Jun 26, 2015
	 * @Time: 7:28:46 PM
	 * @param username
	 * @param password
	 * @return
	 */
	public static final String getLoginUrl(final String username, final String password) {
		return LGJConstant.LoginUrl + "userName=" + username + "&password=" + password;
	}
	
	/**
	 * @Description: 获取用户列表URL
	 * @Date: Jun 26, 2015
	 * @Time: 7:28:30 PM
	 * @param sourceDomain
	 * @param page
	 * @param time
	 * @return
	 */
	public static final String getUserListUrl(final String sourceDomain, final int page) {
		return sourceDomain + LGJConstant.UserListUri + "page=" + page + "&_=" + System.currentTimeMillis();
	}
	
	/**
	 * @Description: 
	 * @Date: Jun 29, 2015
	 * @Time: 5:12:54 PM
	 * @param sourceDomain
	 * @param page
	 * @return
	 */
	public static final String getActivityTypeListUrl(final String sourceDomain, final int page) {
		return sourceDomain + LGJConstant.ActivityTypeListUri + "page=" + page;
	}
	
	/**
	 * @Description: 
	 * @Date: Jun 29, 2015
	 * @Time: 5:13:41 PM
	 * @param sourceDomain
	 * @param page
	 * @param time
	 * @return
	 */
	public static final String getActivityListUrl(final String sourceDomain, final int page) {
		return sourceDomain + LGJConstant.ActivityListUri + "offset=" + (page-1)*5 + "&limit=5&t=" + System.currentTimeMillis();
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 3, 2015
	 * @Time: 10:21:51 AM
	 * @param sourceDomain
	 * @param id
	 * @return
	 */
	public static final String getActivityDetailUrl(final String sourceDomain, final long id) {
		return sourceDomain + LGJConstant.ActivityDetailUri + id;
	}
	
	/**
	 * @Description: 
	 * @Date: Jun 30, 2015
	 * @Time: 8:31:59 PM
	 * @param sourceDomain
	 * @param page
	 * @return
	 */
	public static final String getActivityCommentListUrl(final String sourceDomain, final int page) {
		return sourceDomain + LGJConstant.ActivityCommentListUri + System.currentTimeMillis() + "&page=" + page;
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 10:03:11 AM
	 * @param sourceDomain
	 * @return
	 */
	public static final String getForumCategoryListUrl(final String sourceDomain) {
		return sourceDomain + LGJConstant.ForumCategoryListUri;
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 2:43:59 PM
	 * @param sourceDomain
	 * @param page
	 * @return
	 */
	public static final String getForumListUrl(final String sourceDomain, final int page) {
		return sourceDomain + LGJConstant.ForumListUri + System.currentTimeMillis() + "&page=" + page;
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 3:44:28 PM
	 * @param sourceDomain
	 * @param page
	 * @return
	 */
//	public static final String getForumCommentListUrl(final String sourceDomain, final int page) {
//		return sourceDomain + LGJConstant.ForumCommentListUri + System.currentTimeMillis() + "&page=" + page;
//	}
	
	/**
	 * @Description: 
	 * @Date: Jul 6, 2015
	 * @Time: 9:28:36 PM
	 * @param sourceDomain
	 * @param id
	 * @return
	 */
	public static final String getForumDetailUrl(final String sourceDomain, final long id) {
		return sourceDomain + LGJConstant.ForumDetailUri + id;
	}
}