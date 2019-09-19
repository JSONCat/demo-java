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

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.CommonConstant.PageLimit;
import com.sas.core.constant.LGJConstant;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.dto.PageData;
import com.sas.core.dto.Paginator;
import com.sas.core.meta.LGJ.LGJActivity;
import com.sas.core.meta.LGJ.LGJActivityBatch;
import com.sas.core.meta.LGJ.LGJActivityComment;
import com.sas.core.meta.LGJ.LGJActivityDetail;
import com.sas.core.meta.LGJ.LGJActivityType;
import com.sas.core.meta.LGJ.LGJTask;
import com.sas.core.util.HtmlUtil;
import com.sas.core.util.IOUtil;
import com.sas.core.util.JsonUtil;
import com.sas.core.util.TimeUtil;
import com.sas.core.util.LGJ.TaskBaseUtil;

public class TaskActivityUtil {
	
	private static final Logger logger = Logger.getLogger(TaskActivityUtil.class);

	/**
	 * @Description: 将分类Map转换成对象
	 * @Date: Jun 29, 2015
	 * @Time: 3:43:21 PM
	 * @param lgjTask
	 * @param lgjActivityTypeMap
	 * @return
	 */
	public static final LGJActivityType convertLGJActivityTypeMapToLGJActivityType(final LGJTask lgjTask, final Map<String, Object> lgjActivityTypeMap) {
		return new LGJActivityType(lgjTask.getId(),
				lgjActivityTypeMap.get(LGJConstant.ActivityCount) == null ? 0 : ((Number) lgjActivityTypeMap.get(LGJConstant.ActivityCount)).intValue(),
				lgjActivityTypeMap.get(LGJConstant.ClubId) == null ? 0L : ((Number) lgjActivityTypeMap.get(LGJConstant.ClubId)).longValue(),
				lgjActivityTypeMap.get(LGJConstant.CreateDate) == null ? 0L : ((Number) lgjActivityTypeMap.get(LGJConstant.CreateDate)).longValue(),
				lgjActivityTypeMap.get(LGJConstant.HotFlag) == null ? "" : (String) lgjActivityTypeMap.get(LGJConstant.HotFlag),
				lgjActivityTypeMap.get(LGJConstant.IconUrl) == null ? "" : (String) lgjActivityTypeMap.get(LGJConstant.IconUrl),
				lgjActivityTypeMap.get(LGJConstant.LabelId) == null ? 0L : ((Number) lgjActivityTypeMap.get(LGJConstant.LabelId)).longValue(),
				lgjActivityTypeMap.get(LGJConstant.ModifyDate) == null ? 0L : ((Number) lgjActivityTypeMap.get(LGJConstant.ModifyDate)).longValue(),
				lgjActivityTypeMap.get(LGJConstant.Name) == null ? "" : (String) lgjActivityTypeMap.get(LGJConstant.Name),
				lgjActivityTypeMap.get(LGJConstant.SortId) == null ? 0L : ((Number) lgjActivityTypeMap.get(LGJConstant.SortId)).longValue(),
				lgjActivityTypeMap.get(LGJConstant.State) == null ? 'U' : ((String) lgjActivityTypeMap.get(LGJConstant.State)).charAt(0));
	}
	
	/**
	 * @Description: 
	 * @Date: Jun 30, 2015
	 * @Time: 2:55:33 PM
	 * @param lgjTask
	 * @param lgjActivityMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final LGJActivityDetail convertLGJActivityDetailMapToLGJActivityDetail(final LGJTask lgjTask, final Map<String, Object> lgjActivityMap) {
		final LGJActivity lgjActivity = new LGJActivity(lgjTask.getId(),
				lgjActivityMap.get(LGJConstant.ActivityId) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.ActivityId)).longValue(),
				lgjActivityMap.get(LGJConstant.ActivityIndex) == null ? "" : (String) lgjActivityMap.get(LGJConstant.ActivityIndex),
				lgjActivityMap.get(LGJConstant.ActivityIndexMap) == null ? "" : ((Map<String, Object>) lgjActivityMap.get(LGJConstant.ActivityIndexMap)).toString(),
				lgjActivityMap.get(LGJConstant.ActivitySnapshortId) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.ActivitySnapshortId)).longValue(),
				lgjActivityMap.get(LGJConstant.ActivityStatus) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.ActivityStatus)).intValue(),
				lgjActivityMap.get(LGJConstant.AdvanceTime) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.AdvanceTime)).longValue(),
				lgjActivityMap.get(LGJConstant.Amount) == null ? 0.00 : ((Number) lgjActivityMap.get(LGJConstant.Amount)).doubleValue(),
				lgjActivityMap.get(LGJConstant.ApplicantPeoples) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.ApplicantPeoples)).intValue(),
				lgjActivityMap.get(LGJConstant.ApplyConfig) == null ? "" : (String) lgjActivityMap.get(LGJConstant.ApplyConfig),
				lgjActivityMap.get(LGJConstant.BailPercentage) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.BailPercentage)).intValue(),
				lgjActivityMap.get(LGJConstant.BatchCount) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.BatchCount)).intValue(),
				lgjActivityMap.get(LGJConstant.BeginTime) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.BeginTime)).longValue(),
				lgjActivityMap.get(LGJConstant.CaptainAvatar) == null ? "" : (String) lgjActivityMap.get(LGJConstant.CaptainAvatar),
				lgjActivityMap.get(LGJConstant.CaptainId) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.CaptainId)).longValue(),
				lgjActivityMap.get(LGJConstant.CaptainNickname) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.CaptainNickname)),
				lgjActivityMap.get(LGJConstant.Category) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.Category)).intValue(),
				lgjActivityMap.get(LGJConstant.Cautions) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.Cautions)),
				lgjActivityMap.get(LGJConstant.ChangeNote) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.ChangeNote)),
				lgjActivityMap.get(LGJConstant.ClubId) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.ClubId)).longValue(),
				lgjActivityMap.get(LGJConstant.Cover) == null ? "" : (String) lgjActivityMap.get(LGJConstant.Cover),
				lgjActivityMap.get(LGJConstant.CreateTime) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.CreateTime)).longValue(),
				lgjActivityMap.get(LGJConstant.CreateUserId) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.CreateUserId)).longValue(),
				lgjActivityMap.get(LGJConstant.CreateUserNickname) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.CreateUserNickname)),
				lgjActivityMap.get(LGJConstant.CurrentBatchJsonInfo) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.CurrentBatchJsonInfo)),
				lgjActivityMap.get(LGJConstant.Days) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.Days)).intValue(),
				lgjActivityMap.get(LGJConstant.Deadline) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.Deadline)).longValue(),
				lgjActivityMap.get(LGJConstant.Destination) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.Destination)),
				lgjActivityMap.get(LGJConstant.DetailedCosts) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.DetailedCosts)),
				lgjActivityMap.get(LGJConstant.Difficulty) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.Difficulty)).intValue(),
				lgjActivityMap.get(LGJConstant.Disclaimer) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.Disclaimer)),
				lgjActivityMap.get(LGJConstant.EndProvince) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.EndProvince)).intValue(),
				lgjActivityMap.get(LGJConstant.EndCity) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.EndCity)).intValue(),
				lgjActivityMap.get(LGJConstant.EndCountry) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.EndCountry)).intValue(),
				lgjActivityMap.get(LGJConstant.EndTime) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.EndTime)).longValue(),
				lgjActivityMap.get(LGJConstant.Equipment) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.Equipment)),
				lgjActivityMap.get(LGJConstant.Itinerary) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.Itinerary)),
				lgjActivityMap.get(LGJConstant.Latitude) == null ? 0.00 : ((Number) lgjActivityMap.get(LGJConstant.Latitude)).doubleValue(),
				lgjActivityMap.get(LGJConstant.Longitude) == null ? 0.00 : ((Number) lgjActivityMap.get(LGJConstant.Longitude)).doubleValue(),
				lgjActivityMap.get(LGJConstant.MaxPeoples) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.MaxPeoples)).intValue(),
				lgjActivityMap.get(LGJConstant.MinAmount) == null ? 0.00 : ((Number) lgjActivityMap.get(LGJConstant.MinAmount)).doubleValue(),
				lgjActivityMap.get(LGJConstant.OnlineCharge) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.OnlineCharge)).intValue(),
				lgjActivityMap.get(LGJConstant.Phone) == null ? "" : (String) lgjActivityMap.get(LGJConstant.Phone),
				lgjActivityMap.get(LGJConstant.Pictures) == null ? "" : (String) lgjActivityMap.get(LGJConstant.Pictures),
				lgjActivityMap.get(LGJConstant.PitcureArray) == null ? "" : ((List<String>) lgjActivityMap.get(LGJConstant.PitcureArray)).toString(),
				lgjActivityMap.get(LGJConstant.PrepaymentAmount) == null ? 0.00 : ((Number) lgjActivityMap.get(LGJConstant.PrepaymentAmount)).doubleValue(),
				lgjActivityMap.get(LGJConstant.SortId) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.SortId)).longValue(),
				lgjActivityMap.get(LGJConstant.SortedPictures) == null ? "" : (String) lgjActivityMap.get(LGJConstant.SortedPictures),
				lgjActivityMap.get(LGJConstant.Spot) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.Spot)),
				lgjActivityMap.get(LGJConstant.StartAddress) == null ? "" : (String) lgjActivityMap.get(LGJConstant.StartAddress),
				lgjActivityMap.get(LGJConstant.StartCity) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.StartCity)).intValue(),
				lgjActivityMap.get(LGJConstant.StartCountry) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.StartCountry)).intValue(),
				lgjActivityMap.get(LGJConstant.StartProvince) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.StartProvince)).intValue(),
				lgjActivityMap.get(LGJConstant.Strength) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.Strength)).intValue(),
				lgjActivityMap.get(LGJConstant.Summary) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityMap.get(LGJConstant.Summary)),
				lgjActivityMap.get(LGJConstant.Title) == null ? "" : (String) lgjActivityMap.get(LGJConstant.Title),
				lgjActivityMap.get(LGJConstant.TopStatus) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.TopStatus)).intValue(),
				lgjActivityMap.get(LGJConstant.TopTime) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.TopTime)).longValue(),
				lgjActivityMap.get(LGJConstant.Type) == null ? "" : (String) lgjActivityMap.get(LGJConstant.Type),
				lgjActivityMap.get(LGJConstant.TypeCount) == null ? 0 : ((Number) lgjActivityMap.get(LGJConstant.TypeCount)).intValue(),
				lgjActivityMap.get(LGJConstant.UpdateTime) == null ? 0L : ((Number) lgjActivityMap.get(LGJConstant.UpdateTime)).longValue());
		final List<LGJActivityBatch> lgjActivityBatchList = TaskActivityUtil.convertLGJActivityBatchMapListToLGJActivityBatchList(lgjTask,
				(List<Map<String, Object>>) lgjActivityMap.get(LGJConstant.BatchList) == null ? new ArrayList<Map<String, Object>>(0)
						: (List<Map<String, Object>>) lgjActivityMap.get(LGJConstant.BatchList));
		return new LGJActivityDetail(lgjActivity, lgjActivityBatchList);
	}
	
	/**
	 * @Description: 
	 * @Date: Jun 30, 2015
	 * @Time: 4:39:48 PM
	 * @param lgjTask
	 * @param lgjActivityBatchMapList
	 * @return
	 */
	public static final List<LGJActivityBatch> convertLGJActivityBatchMapListToLGJActivityBatchList(final LGJTask lgjTask,
			final List<Map<String, Object>> lgjActivityBatchMapList) {
		List<LGJActivityBatch> lgjActivityBatchList = new ArrayList<LGJActivityBatch>();
		for (Map<String, Object> lgjActivityBatchMap : lgjActivityBatchMapList) {
			lgjActivityBatchList.add(new LGJActivityBatch(lgjTask.getId(),
					lgjActivityBatchMap.get(LGJConstant.Id) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.Id)).intValue(),
					lgjActivityBatchMap.get(LGJConstant.ActivityId) == null ? 0L : ((Number) lgjActivityBatchMap.get(LGJConstant.ActivityId)).longValue(),
					lgjActivityBatchMap.get(LGJConstant.Amount) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.Amount)).doubleValue(),
					lgjActivityBatchMap.get(LGJConstant.AppliedPeoples) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.AppliedPeoples)).intValue(),
					lgjActivityBatchMap.get(LGJConstant.BeginTime) == null ? 0L : ((Number) lgjActivityBatchMap.get(LGJConstant.BeginTime)).longValue(),
					lgjActivityBatchMap.get(LGJConstant.ClubId) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.ClubId)).intValue(),
					lgjActivityBatchMap.get(LGJConstant.Deadline) == null ? 0L : ((Number) lgjActivityBatchMap.get(LGJConstant.Deadline)).longValue(),
					lgjActivityBatchMap.get(LGJConstant.EndTime) == null ? 0L : ((Number) lgjActivityBatchMap.get(LGJConstant.EndTime)).longValue(),
					lgjActivityBatchMap.get(LGJConstant.MaxPeoples) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.MaxPeoples)).intValue(),
					lgjActivityBatchMap.get(LGJConstant.OrderItemPrice) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.OrderItemPrice)).intValue(),
					lgjActivityBatchMap.get(LGJConstant.PaymentCategory) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.PaymentCategory)).intValue(),
					lgjActivityBatchMap.get(LGJConstant.PrepaymentAmount) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.PrepaymentAmount)).doubleValue(),
					lgjActivityBatchMap.get(LGJConstant.State) == null ? 0 : ((Number) lgjActivityBatchMap.get(LGJConstant.State)).intValue()));
		}
		return lgjActivityBatchList;
	}
	
	/**
	 * @Description: 
	 * @Date: Jun 30, 2015
	 * @Time: 8:49:01 PM
	 * @param lgjTask
	 * @param lgjActivityCommentMap
	 * @return
	 */
	public static final LGJActivityComment convertLGJActivityCommentMapToLGJActivityComment(final LGJTask lgjTask,
			final Map<String, Object> lgjActivityCommentMap) {
		return new LGJActivityComment(lgjTask.getId(),
				lgjActivityCommentMap.get(LGJConstant.CmmtId) == null ? 0L : ((Number) lgjActivityCommentMap.get(LGJConstant.CmmtId)).longValue(),
				lgjActivityCommentMap.get(LGJConstant.Author) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityCommentMap.get(LGJConstant.Author)),
				lgjActivityCommentMap.get(LGJConstant.CmmtTime) == null ? 0L : ((Number) lgjActivityCommentMap.get(LGJConstant.CmmtTime)).longValue(),
				lgjActivityCommentMap.get(LGJConstant.Content) == null ? "" :  HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityCommentMap.get(LGJConstant.Content)),
				lgjActivityCommentMap.get(LGJConstant.EntityBody) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityCommentMap.get(LGJConstant.EntityBody)),
				lgjActivityCommentMap.get(LGJConstant.EntityId) == null ? 0L : ((Number) lgjActivityCommentMap.get(LGJConstant.EntityId)).longValue(),
				lgjActivityCommentMap.get(LGJConstant.EntityUserId) == null ? 0L : ((Number) lgjActivityCommentMap.get(LGJConstant.EntityUserId)).longValue(),
				lgjActivityCommentMap.get(LGJConstant.ImgCount) == null ? 0 : ((Number) lgjActivityCommentMap.get(LGJConstant.ImgCount)).intValue(),
				lgjActivityCommentMap.get(LGJConstant.ImgList).toString() == null ? "" : (lgjActivityCommentMap.get(LGJConstant.ImgList)).toString(),
				lgjActivityCommentMap.get(LGJConstant.Summary) == null ? "" : HtmlUtil.removeMobileUTF8MB4Emotions((String) lgjActivityCommentMap.get(LGJConstant.Summary)));
	}
	
	/**
	 * @Description: 获取活动分类列表
	 * @Date: Jun 29, 2015
	 * @Time: 3:54:18 PM
	 * @param lgjTask
	 * @param page
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static final PageData<LGJActivityType> getLGJActivityTypeList(final LGJTask lgjTask, final int page) throws HttpException, IOException {
		final HttpClient httpClient = new HttpClient();
		final String url = TaskBaseUtil.getActivityTypeListUrl(lgjTask.getSourceDomain(), page);
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
			List<LGJActivityType> typeList = new ArrayList<LGJActivityType>(list.size());
			for (Map<String, Object> map : list) {
				typeList.add(convertLGJActivityTypeMapToLGJActivityType(lgjTask, map));
			}
			getMethod.abort();
			final Paginator paginator = new Paginator(PageLimit.Ten.limit, (Integer) resultMap.get(LGJConstant.Total));
			return new PageData<LGJActivityType>(typeList, paginator);
		}
	}
	
	/**
	 * @Description: 
	 * @Date: Jun 30, 2015
	 * @Time: 4:39:54 PM
	 * @param lgjTask
	 * @param page
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static final List<LGJActivityDetail> getLGJActivityDetailList(final LGJTask lgjTask, final int page) throws HttpException, IOException {
		final HttpClient httpClient = new HttpClient();
		final String url = TaskBaseUtil.getActivityListUrl(lgjTask.getSourceDomain(), page);
		PostMethod postMethod = new PostMethod(url);
		TaskBaseUtil.setRequestHeaderAndCookies(null, postMethod, lgjTask);
		if (httpClient.executeMethod(postMethod) != 200) {
			postMethod.abort();
			logger.error("Failed to executeMethod with url : " + url + ", StatusLine : " + postMethod.getStatusLine());
			return null;
		} else {
			String returnData = null;
			try{
				returnData = IOUtil.readTextFromHttpURL(postMethod.getResponseBodyAsStream(), Encoding.UTF8);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			final Map<String, Object> returnMap = JsonUtil.getObject(returnData, Map.class);
			final Integer code = (Integer) returnMap.get(LGJConstant.Code);
			if (code == null || !code.equals(200)) {
				logger.error("Failed to getLGJUserDetailList with sourceDomain : " + lgjTask.getSourceDomain() + " & page : " + page);
				return null;
			}
			final List<Map<String, Object>> list = (List<Map<String, Object>>) returnMap.get(LGJConstant.Result);
			final List<LGJActivityDetail> activityDetailList = new ArrayList<LGJActivityDetail>(list.size());
			for (Map<String, Object> map : list) {
				activityDetailList.add(convertLGJActivityDetailMapToLGJActivityDetail(lgjTask, map));
			}
			postMethod.abort();
			return TaskActivityUtil.setActivityContent(lgjTask, activityDetailList);
			//final Paginator paginator = new Paginator(PageLimit.Ten.limit, (Integer) resultMap.get(LGJConstant.Total));
			//return new PageData<LGJActivityDetail>(activityDetailList, paginator);
		}
	}
	
	/**
	 * @Description: 
	 * @Date: Jul 3, 2015
	 * @Time: 10:40:50 AM
	 * @param lgjTask
	 * @param detailList
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static final List<LGJActivityDetail> setActivityContent(final LGJTask lgjTask, List<LGJActivityDetail> detailList) throws HttpException, IOException {
		for (LGJActivityDetail detail : detailList) {
			HttpClient httpClient = new HttpClient();
			final String url = TaskBaseUtil.getActivityDetailUrl(lgjTask.getSourceDomain(), detail.getLgjActivity().getSourceId());
			GetMethod getMethod = new GetMethod(url);
			if (httpClient.executeMethod(getMethod) != 200) {
				getMethod.abort();
				logger.error("Failed to executeMethod with url : " + url + ", StatusLine : " + getMethod.getStatusLine());
				continue;
			} else {
				final String returnData = getMethod.getResponseBodyAsString();
				final Document doc = Jsoup.parse(returnData);
				final Elements elements = doc.getElementsByAttributeValue("class", "text");
				String content = "";
				for (Element element : elements) {
					content = content + element.html();
				}
				detail.getLgjActivity().setContent(HtmlUtil.removeMobileUTF8MB4Emotions(content));
			}
		}
		return detailList;
	}
	
	/**
	 * @Description: 
	 * @Date: Jun 30, 2015
	 * @Time: 8:34:51 PM
	 * @param lgjTask
	 * @param page
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static final PageData<LGJActivityComment> getLGJActivityCommentList(final LGJTask lgjTask, final int page) throws HttpException, IOException {
		final HttpClient httpClient = new HttpClient();
		final String url = TaskBaseUtil.getActivityCommentListUrl(lgjTask.getSourceDomain(), page);
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
			final List<LGJActivityComment> activityCommentList = new ArrayList<LGJActivityComment>(list.size());
			for (Map<String, Object> map : list) {
				activityCommentList.add(convertLGJActivityCommentMapToLGJActivityComment(lgjTask, map));
			}
			getMethod.abort();
			final Paginator paginator = new Paginator(PageLimit.Ten.limit, (Integer) resultMap.get(LGJConstant.Total));
			return new PageData<LGJActivityComment>(activityCommentList, paginator);
		}
	}
	
	/********************
	 * 获取省份信息
	 * @param province
	 * @return
	 */
	public static final String getLGJProvince(final int province, String address)
	{
		if(address == null){
			address = "";
		}
		final int[] indexs = new int[]{110000, 120000, 130000, 140000, 150000, 210000, 220000,
				230000, 310000, 320000, 330000, 340000, 350000, 360000, 370000, 410000, 420000, 
				430000, 440000, 450000, 460000, 500000, 510000, 520000, 530000, 540000, 610000,
				620000, 630000, 640000, 650000, 710000, 810000, 820000};
		final String[] provinces = new String[]{"北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江",
			"上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南", "广东", "广西", "海南", "重庆",
			"四川", "贵州", "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆", "台湾", "香港", "澳门"};
		for(int i=0; i<indexs.length; i++){
			if(indexs[i] == province || address.contains(provinces[i])){
				return provinces[i];
			}
		}
		return "其它";
	}
	
	/***************
	 * 格式化名字
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static final String formatStyleClassName(final long startTime, final long endTime){
		if(startTime < 10000){
			if(endTime < 10000){
				return "默认";
			}
			return TimeUtil.formatDate(endTime, TimeFormat.MMDD);
		}else if(endTime < 10000){
			return TimeUtil.formatDate(startTime, TimeFormat.MMDD);
		}else{
			return TimeUtil.formatDate(startTime, TimeFormat.MMDD) +"-"+ TimeUtil.formatDate(endTime, TimeFormat.MMDD);
		}
	}
}