package com.sas.core.util.LGJ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sas.core.constant.CommonConstant.PageLimit;
import com.sas.core.constant.LGJConstant;
import com.sas.core.dto.PageData;
import com.sas.core.dto.Paginator;
import com.sas.core.meta.LGJ.LGJForum;
import com.sas.core.meta.LGJ.LGJForumCategory;
import com.sas.core.meta.LGJ.LGJForumComment;
import com.sas.core.meta.LGJ.LGJTask;
import com.sas.core.util.HtmlUtil;
import com.sas.core.util.JsonUtil;
import com.sas.core.util.LGJ.TaskBaseUtil;

public class TaskForumUtil {
	
	private static final Logger logger = Logger.getLogger(TaskForumUtil.class);

	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 10:09:11 AM
	 * @param lgjTask
	 * @param lgjForumCategoryMap
	 * @return
	 */
	public static final LGJForumCategory convertLGJForumCategoryMapToLGJForumCategory(final LGJTask lgjTask,
			final Map<String, Object> lgjForumCategoryMap) {
		return new LGJForumCategory(lgjTask.getId(),
				lgjForumCategoryMap.get(LGJConstant.Id) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.Id)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.ClubId) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.ClubId)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.Cover) == null ? "" : (String) lgjForumCategoryMap.get(LGJConstant.Cover),
				lgjForumCategoryMap.get(LGJConstant.CreateTime) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.CreateTime)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.ForumCount) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.ForumCount)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.ForumReplyCount) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.ForumReplyCount)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.ForumViewCount) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.ForumViewCount)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.GoodForumCount) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.GoodForumCount)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.Intro) == null ? "" : (String) lgjForumCategoryMap.get(LGJConstant.Intro),
				lgjForumCategoryMap.get(LGJConstant.LastForumCreateTime) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.LastForumCreateTime)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.LastForumId) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.LastForumId)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.LastForumReplyId) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.LastForumReplyId)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.LastForumReplyTime) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.LastForumReplyTime)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.LastForumReplyUserId) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.LastForumReplyUserId)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.LastForumUserId) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.LastForumUserId)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.Name) == null ? "" : (String) lgjForumCategoryMap.get(LGJConstant.Name),
				lgjForumCategoryMap.get(LGJConstant.ParentId) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.ParentId)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.Sort) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.Sort)).longValue(),
				lgjForumCategoryMap.get(LGJConstant.UserId) == null ? 0L : ((Number) lgjForumCategoryMap.get(LGJConstant.UserId)).longValue());
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 2:55:11 PM
	 * @param lgjTask
	 * @param lgjForumMap
	 * @return
	 */
	public static final LGJForum convertLGJForumMapToLGJForum(final LGJTask lgjTask, final Map<String, Object> lgjForumMap) {
		return new LGJForum(lgjTask.getId(),
				lgjForumMap.get(LGJConstant.Id) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.Id)).longValue(),
				lgjForumMap.get(LGJConstant.CategoryId) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.CategoryId)).longValue(),
				lgjForumMap.get(LGJConstant.CategoryName) == null ? "" : (String) lgjForumMap.get(LGJConstant.CategoryName),
				lgjForumMap.get(LGJConstant.ClubId) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.ClubId)).longValue(),
				lgjForumMap.get(LGJConstant.CreateTime) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.CreateTime)).longValue(),
				lgjForumMap.get(LGJConstant.FirstReplyId) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.FirstReplyId)).longValue(),
				lgjForumMap.get(LGJConstant.GoodState) == null ? 0 : ((Number) lgjForumMap.get(LGJConstant.GoodState)).intValue(),
				lgjForumMap.get(LGJConstant.ItemAttachmentCount) == null ? 0 : ((Number) lgjForumMap.get(LGJConstant.ItemAttachmentCount)).intValue(),
				lgjForumMap.get(LGJConstant.ItemCount) == null ? 0 : ((Number) lgjForumMap.get(LGJConstant.ItemCount)).intValue(),
				lgjForumMap.get(LGJConstant.ItemImgCount) == null ? 0 : ((Number) lgjForumMap.get(LGJConstant.ItemImgCount)).intValue(),
				lgjForumMap.get(LGJConstant.LastGoodTime) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.LastGoodTime)).longValue(),
				lgjForumMap.get(LGJConstant.LastReplyName) == null ? "" : (String) lgjForumMap.get(LGJConstant.LastReplyName),
				lgjForumMap.get(LGJConstant.LastReplyTime) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.LastReplyTime)).longValue(),
				lgjForumMap.get(LGJConstant.LastReplyUserId) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.LastReplyUserId)).longValue(),
				lgjForumMap.get(LGJConstant.LastTopTime) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.LastTopTime)).longValue(),
				lgjForumMap.get(LGJConstant.LastUpdateTime) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.LastUpdateTime)).longValue(),
				lgjForumMap.get(LGJConstant.LastUpdateUserId) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.LastUpdateUserId)).longValue(),
				lgjForumMap.get(LGJConstant.ReplyCount) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.ReplyCount)).longValue(),
				lgjForumMap.get(LGJConstant.Title) == null ? "" : (String) lgjForumMap.get(LGJConstant.Title),
				lgjForumMap.get(LGJConstant.TopState) == null ? 0 : ((Number) lgjForumMap.get(LGJConstant.TopState)).intValue(),
				lgjForumMap.get(LGJConstant.UserAvatar) == null ? "" : (String) lgjForumMap.get(LGJConstant.UserAvatar),
				lgjForumMap.get(LGJConstant.UserId) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.UserId)).longValue(),
				lgjForumMap.get(LGJConstant.UserNickName) == null ? "" : (String) lgjForumMap.get(LGJConstant.UserNickName),
				lgjForumMap.get(LGJConstant.ViewCount) == null ? 0L : ((Number) lgjForumMap.get(LGJConstant.ViewCount)).longValue());
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 3:56:30 PM
	 * @param lgjTask
	 * @param lgjForumCommentMap
	 * @return
	 */
	public static final LGJForumComment convertLGJForumCommentMapToLGJForumComment(final LGJTask lgjTask,
			final Map<String, Object> lgjForumCommentMap) {
		return new LGJForumComment(lgjTask.getId(),
				lgjForumCommentMap.get(LGJConstant.CmmtId) == null ? 0L : ((Number) lgjForumCommentMap.get(LGJConstant.CmmtId)).longValue(),
				lgjForumCommentMap.get(LGJConstant.Author) == null ? "" : (String) lgjForumCommentMap.get(LGJConstant.Author),
				lgjForumCommentMap.get(LGJConstant.CmmtTime) == null ? 0L : ((Number) lgjForumCommentMap.get(LGJConstant.CmmtTime)).longValue(),
				lgjForumCommentMap.get(LGJConstant.Content) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjForumCommentMap.get(LGJConstant.Content)),
				lgjForumCommentMap.get(LGJConstant.EntityBody) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjForumCommentMap.get(LGJConstant.EntityBody)),
				lgjForumCommentMap.get(LGJConstant.EntityId) == null ? 0L : ((Number) lgjForumCommentMap.get(LGJConstant.EntityId)).longValue(),
				lgjForumCommentMap.get(LGJConstant.EntityUserId) == null ? 0L : ((Number) lgjForumCommentMap.get(LGJConstant.EntityUserId)).longValue(),
				lgjForumCommentMap.get(LGJConstant.ImgCount) == null ? 0 : ((Number) lgjForumCommentMap.get(LGJConstant.ImgCount)).intValue(),
				lgjForumCommentMap.get(LGJConstant.ImgList).toString() == null ? "" : (lgjForumCommentMap.get(LGJConstant.ImgList)).toString(),
				lgjForumCommentMap.get(LGJConstant.Summary) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjForumCommentMap.get(LGJConstant.Summary)));
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 11:01:40 AM
	 * @param lgjTask
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static final List<LGJForumCategory> getLGJForumCategoryList(final LGJTask lgjTask) throws HttpException, IOException {
		final HttpClient httpClient = new HttpClient();
		final String url = TaskBaseUtil.getForumCategoryListUrl(lgjTask.getSourceDomain());
		PostMethod postMethod = new PostMethod(url);
		TaskBaseUtil.setRequestHeaderAndCookies(null, postMethod, lgjTask);
		if (httpClient.executeMethod(postMethod) != 200) {
			postMethod.abort();
			logger.error("Failed to executeMethod with url : " + url + ", StatusLine : " + postMethod.getStatusLine());
			return null;
		} else {
			final String returnData = postMethod.getResponseBodyAsString();
			final Map<String, Object> returnMap = JsonUtil.getObject(returnData, Map.class);
			final Integer code = (Integer) returnMap.get(LGJConstant.Code);
			if (code == null || !code.equals(200)) {
				logger.error("Failed to getLGJForumCategoryList with sourceDomain : " + lgjTask.getSourceDomain());
				return null;
			}
			final List<Map<String, Object>> list = (List<Map<String, Object>>) returnMap.get(LGJConstant.Result);
			final List<LGJForumCategory> lgjForumCategoryList = new ArrayList<LGJForumCategory>();
			for (Map<String, Object> map : list) {
//				lgjForumCategoryList.add(convertLGJForumCategoryMapToLGJForumCategory(lgjTask, (Map<String, Object>) map.get(LGJConstant.ParentCategory)));
				List<Map<String, Object>> childrenCategoryMapList = (List<Map<String, Object>>) map.get(LGJConstant.ChildCategories);
				if (childrenCategoryMapList != null) {
					for (Map<String, Object> childrenCategoryMap : childrenCategoryMapList) {
						lgjForumCategoryList.add(convertLGJForumCategoryMapToLGJForumCategory(lgjTask, childrenCategoryMap));
					}
				}
			}
			postMethod.abort();
			return lgjForumCategoryList;
		}
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 3:13:11 PM
	 * @param lgjTask
	 * @param page
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static final PageData<LGJForum> getLGJForumList(final LGJTask lgjTask, final int page) throws HttpException, IOException {
		final HttpClient httpClient = new HttpClient();
		final String url = TaskBaseUtil.getForumListUrl(lgjTask.getSourceDomain(), page);
		GetMethod getMethod = new GetMethod(url);
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
			final List<LGJForum> lgjForumList = new ArrayList<LGJForum>();
			for (Map<String, Object> map : list) {
				lgjForumList.add(convertLGJForumMapToLGJForum(lgjTask, map));
			}
//			ThreadUtil.execute(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						TaskForumUtil.getLGJForumFirstReplyList(lgjTask, lgjForumList);
//					} catch (HttpException e) {
//						logger.error("Failed to getLGJForumFirstReplyList with HttpException : " + e, e);
//					} catch (IOException e) {
//						logger.error("Failed to getLGJForumFirstReplyList with IOExceptions : " + e, e);
//					}
//				}
//			});
			getMethod.abort();
			final Paginator paginator = new Paginator(PageLimit.Ten.limit, (Integer) resultMap.get(LGJConstant.Total));
			return new PageData<LGJForum>(lgjForumList, paginator);
		}
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 6, 2015
	 * @Time: 9:37:02 PM
	 * @param lgjTask
	 * @param lgjForumList
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static final List<LGJForumComment> getLGJForumFirstReplyList(final LGJTask lgjTask, final List<LGJForum> lgjForumList) throws HttpException, IOException {
		final List<LGJForumComment> commentList =  new ArrayList<LGJForumComment>(lgjForumList.size());
		for (LGJForum forum : lgjForumList) {
			HttpClient httpClient = new HttpClient();
			final String url = TaskBaseUtil.getForumDetailUrl(lgjTask.getSourceDomain(), forum.getSourceId());
			GetMethod getMethod = new GetMethod(url);
			if (httpClient.executeMethod(getMethod) != 200) {
				getMethod.abort();
				logger.error("Failed to executeMethod with url : " + url + ", StatusLine : " + getMethod.getStatusLine());
				continue;
			} else {
				String returnData = getMethod.getResponseBodyAsString();
				final Document doc = Jsoup.parse(returnData);
				final Elements elements = doc.getElementsByAttributeValue("class", "text");
				final StringBuilder sb = new StringBuilder("");
				if(elements.size() > 0){
					for (Element element : elements) {
						sb.append(element.html());
					}
				}else{//容错代码
					final String str1 = "<div class=\"text\">", str2 = "<div class=\"comment-post clearfix\" id=\"subCommentPost\">";
					final int index1 = returnData.indexOf(str1);
					final int index2 = returnData.indexOf(str2);
					if(index2 > index1 && index1 > 0){
						returnData = returnData.substring(index1+str1.length(), index2).trim();
						sb.append(returnData.substring(0, returnData.length()-"</div>".length()));
					}
				}
				commentList.add(new LGJForumComment(lgjTask.getId(), 0L, forum.getUserNickName(), forum.getCreateTime(), sb.toString(), "", forum
						.getSourceId(), forum.getUserId(), 0, "", ""));
			}
		}
		return commentList;
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 1, 2015
	 * @Time: 3:57:48 PM
	 * @param lgjTask
	 * @param page
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
//	@SuppressWarnings("unchecked")
//	public static final PageData<LGJForumComment> getLGJForumCommentList(final LGJTask lgjTask, final int page) throws HttpException, IOException {
//		final HttpClient httpClient = new HttpClient();
//		httpClient.getState().addCookies(cookies);
//		final String url = TaskBaseUtil.getForumCommentListUrl(lgjTask.getSourceDomain(), page);
//		GetMethod getMethod = new GetMethod(url);
//		TaskBaseUtil.setRequestHeaderAndCookies(getMethod, null, lgjTask);
//		if (httpClient.executeMethod(getMethod) != 200) {
//			getMethod.abort();
//			logger.error("Failed to executeMethod with url : " + url + ", StatusLine : " + getMethod.getStatusLine());
//			return null;
//		} else {
//			final String returnData = getMethod.getResponseBodyAsString();
//			final Map<String, Object> returnMap = JsonUtil.getObject(returnData, Map.class);
//			final Integer code = (Integer) returnMap.get(LGJConstant.Code);
//			if (code == null || !code.equals(200)) {
//				logger.error("Failed to getLGJUserDetailList with sourceDomain : " + lgjTask.getSourceDomain() + " & page : " + page);
//				return null;
//			}
//			final Map<String, Object> resultMap = (Map<String, Object>) returnMap.get(LGJConstant.Result);
//			final List<Map<String, Object>> list = (List<Map<String, Object>>) resultMap.get(LGJConstant.List);
//			final List<LGJForumComment> forumCommentList = new ArrayList<LGJForumComment>(list.size());
//			for (Map<String, Object> map : list) {
//				forumCommentList.add(convertLGJForumCommentMapToLGJForumComment(lgjTask, map));
//			}
//			getMethod.abort();
//			final Paginator paginator = new Paginator(PageLimit.Ten.limit, (Integer) resultMap.get(LGJConstant.Total));
//			return new PageData<LGJForumComment>(forumCommentList, paginator);
//		}
//	}
}