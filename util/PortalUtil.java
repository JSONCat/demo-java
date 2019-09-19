/**
 * 
 */
package com.sas.core.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sas.core.constant.AuthorityConstant.UserAccessRole;
import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.PortalConstant.PortalActivityCategoryType;
import com.sas.core.constant.PortalConstant.PortalCensorState;
import com.sas.core.constant.PortalConstant.SasContentType;
import com.sas.core.constant.SasTrackConstants.UserTrackType;
import com.sas.core.meta.PortalActivityCategory;
import com.sas.core.meta.PortalCaptain;
import com.sas.core.meta.PortalSite;
import com.sas.core.meta.PortalUserFavority;
import com.sas.core.meta.PortalUserStatistic;
import com.sas.core.meta.SasMenuAlbum;
import com.sas.core.meta.SasUserTrack;
import com.sas.core.util.meta.PortalActivityUtil;

/**
 * C端的util
 * @author Administrator
 *
 */
public class PortalUtil {

	/*************************
	 * 解析成Map
	 * @param list
	 * @return
	 */
	public static final Map<PortalActivityCategoryType, List<PortalActivityCategory>> mapByType(final List<PortalActivityCategory> list){
		final Map<PortalActivityCategoryType, List<PortalActivityCategory>> result = new HashMap<PortalActivityCategoryType, List<PortalActivityCategory>>();
		for(final PortalActivityCategory c : list){
			final PortalActivityCategoryType pact = PortalActivityCategoryType.parse(c.getType());
			List<PortalActivityCategory> subList = result.get(pact);
			if(subList == null){
				subList = new LinkedList<PortalActivityCategory>();
			}
			subList.add(c);
			result.put(pact, subList);
		}		
		return result;
	}
	
	/**************
	 * 生成目的地景点的详细地址
	 * @param siteDomain
	 * @param destinationId
	 * @return
	 */
	public static final String createDestinationDetailURL(final long destinationId){		
		return "/destination?id=" + destinationId;
	}
	
	/***************
	 * 创建足迹的像详情信息url
	 * @param track
	 * @return
	 */
	public static final String createDetailURL(final SasUserTrack track){
		final int type = track.getTrackType();
		if(type == UserTrackType.ActivityAdd.type || type == UserTrackType.ActivityApply.type 
				|| type == UserTrackType.ActivityCommentAdd.type){
			return PortalActivityUtil.createActivityDetailPageURLUsingActId(track.getEntityDependenceId());
		}else if(type == UserTrackType.ForumAdd.type || type == UserTrackType.ForumReplyAdd.type){
			return PortalContentShareUtil.createDetailURLByContentId(SasContentType.Forum.type, track.getEntityDependenceId(), false);
		}else if(type == UserTrackType.ArticleAdd.type || type == UserTrackType.ArticleCommentAdd.type){
			return PortalContentShareUtil.createDetailURLByContentId(SasContentType.Article.type, track.getEntityDependenceId(), false);
		}else if(type == UserTrackType.PhotoUpload.type){
			return PortalContentShareUtil.createDetailURLByContentId(SasContentType.AlbumPhoto.type, track.getEntityDependenceId(), false);
		}else if(type == UserTrackType.PortalMatchEntryAdd.type || type == UserTrackType.PortalMatchEntryVote.type){
			return MatchUtil.createMatchEntryDetailURL(track.getEntityDependenceId(), false);
		}else {
			return null;
		}
	}
	
	public static final String createPhotoCommentDetailURL(final SasUserTrack track, final SasMenuAlbum album){
		if(track.getTrackType() == UserTrackType.PhotoCommentAdd.type || track.getTrackType() == UserTrackType.PhotoUpload.type){
			return PortalContentShareUtil.createDetailURLByContentId(SasContentType.AlbumPhoto.type, album.getId(), false);
		}else {
			return null;
		}
	}
	
	/****************
	 * 用省名进行分组
	 * @param allSites
	 * @return
	 */
	public static final Map<String, List<PortalSite>> mapByProvinceName(final List<PortalSite> allSites)
	{
		final Map<String, List<PortalSite>> map = new HashMap<String, List<PortalSite>>();
		for(final PortalSite ps : allSites){
			List<PortalSite> list = map.get(ps.getProvince());
			if(list == null){
				list = new LinkedList<PortalSite>();
			}
			list.add(ps);
			map.put(ps.getProvince(), list);
		}		
		return map;
	}
	
	/********************
	 * 将newId添加到id的头部
	 * @param newId
	 * @param sourceIdString
	 * @param dividerChar
	 * @param maxCount
	 * @return
	 */
	public static final String insertId2Header(final long newId, final String sourceIdString, final DividerChar dividerChar, final int maxCount)
	{
		if(StringUtils.isBlank(sourceIdString)){
			return String.valueOf(newId);
		}
		final StringBuilder result = new StringBuilder(String.valueOf(newId));
		final List<Long> oldIds = CollectionUtils.splitIdArray(sourceIdString, dividerChar);
		int count = 1;
		for(final long oldId : oldIds){
			result.append(dividerChar.chars);
			result.append(String.valueOf(oldId));
			count ++;
			if(maxCount > 0 && count >= maxCount){
				break;
			}			
		}
		return result.toString();
	}
	
	/**************************
	 * 转换成MAP
	 * @param list
	 * @return
	 */
	public static final Map<String, PortalUserFavority> convert2MapByContent(final List<PortalUserFavority> list){
		final Map<String, PortalUserFavority> favoritesMap = new HashMap<String, PortalUserFavority>();
		if(CollectionUtils.isEmpty(list)){
			return favoritesMap;
		}
		for(final PortalUserFavority f : list){
			if(f.getContentType() == SasContentType.AlbumPhoto.type){
				favoritesMap.put(SasContentType.AlbumPhoto.createCacheKey(f.getContentId()), f);
			}else if(f.getContentType() == SasContentType.Forum.type){
				favoritesMap.put(SasContentType.Forum.createCacheKey(f.getContentId()), f);
			}else if(f.getContentType() == SasContentType.Article.type){
				favoritesMap.put(SasContentType.Article.createCacheKey(f.getContentId()), f);
			}else if(f.getContentType() == SasContentType.Activity.type){
				favoritesMap.put(SasContentType.Activity.createCacheKey(f.getContentId()), f);
			}else if(f.getContentType() == SasContentType.Destination.type){
				favoritesMap.put(SasContentType.Destination.createCacheKey(f.getContentId()), f);
			}else if(f.getContentType() == SasContentType.Good.type){
				favoritesMap.put(SasContentType.Good.createCacheKey(f.getContentId()), f);
			}
		}
		return favoritesMap;
	}
		
	/***************
	 * 创建登录链接
	 * @param needRoleToAccess
	 * @return
	 */
	public static final String createLoginUrl(final UserAccessRole needRoleToAccess)
	{
		if(needRoleToAccess == UserAccessRole.Admin){
			return "/admin/login";
		}else{
			return "/login";
		}
	}
	
	/************
	 * 是否是户外领队
	 * @param ps
	 * @param captain
	 * @return
	 */
	public static final boolean isPortalAndSasCaptain(final PortalUserStatistic ps, final PortalCaptain captain)
	{
		return ps.getPublishActivityCount() > 0 || (captain.getCensorState() == PortalCensorState.PassCensor.state);
	}
}
