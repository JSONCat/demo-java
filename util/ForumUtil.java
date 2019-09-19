package com.sas.core.util;

import java.util.*;

import com.sas.core.constant.ForumConstant;
import com.sas.core.constant.TimeConstant.TimeFormat;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import com.sas.core.constant.CommonConstant.ActionAuthority;
import com.sas.core.constant.CommonConstant.BinaryState;
import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.EnvironmentType;
import com.sas.core.constant.ForumConstant.ForumExtConfig;
import com.sas.core.constant.ForumConstant.ForumType;
import com.sas.core.constant.FullTextSearchConstant.FullTextField;
import com.sas.core.constant.MenuConstant.MenuType;
import com.sas.core.constant.SasTrackConstants;
import com.sas.core.constant.ThirdPartConstant.QiNiuImageCompress;
import com.sas.core.constant.ThirdPartConstant.QiNiuSpace;
import com.sas.core.constant.UserConstant;
import com.sas.core.dto.AttachmentFile;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.dto.CDNMonitorDataCache;
import com.sas.core.meta.SasMenuBbsForum;
import com.sas.core.meta.SasMenuBbsForumCategory;
import com.sas.core.meta.SasMenuBbsForumReply;
import com.sas.core.meta.SasMenuBbsSetting;
import com.sas.core.meta.User;
import com.sas.core.service.EnvironmentService;

/*************
 * Forum util
 * @author zhuliming
 *
 */
public class ForumUtil {
	
	private static final Logger logger = Logger.getLogger(ForumUtil.class);
	
	private static final LRUMap userAddTopicTimeCachePerDay = new LRUMap(100000); //sasUser每天发帖的次数控制
	private static final LRUMap userReplyTopicTimeCachePerDay = new LRUMap(100000); //sasUser每天回复帖子的次数控制
  
  /**
   * 增加用户id发帖次数
   *
   * @param id sasUserId
   */
  public static final synchronized void increaseAddTopicTime(final long id){
    if (id < 1){
      return;
    }
    final String key = id + "_" + TimeUtil.formatDate(System.currentTimeMillis(), TimeFormat.YYYYMMDD);
    final Long times = (Long)userAddTopicTimeCachePerDay.get(key);
    if (times != null){
      userAddTopicTimeCachePerDay.put(key, times + 1L);
    }else {
      userAddTopicTimeCachePerDay.put(key, 1L);
    }
  }
  
  /**
   * 判断发帖是否需要验证码
   * @param id sasUserId
   */
  public static final boolean addTopicNeedCaptcha(final long id){
    if (id < 1){
      return false;
    }
    final String key = id + "_" + TimeUtil.formatDate(System.currentTimeMillis(), TimeFormat.YYYYMMDD);
    final Long times = (Long)userAddTopicTimeCachePerDay.get(key);
    return times != null && times >= ForumConstant.ForumNeedCaptchaTimes.AddTopic.time;
  }
  
  /**
   * 增加用户id回帖次数
   *
   * @param id sasUserId
   */
  public static final synchronized void increaseReplyTopicTime(final long id){
    if (id < 1){
      return;
    }
    final String key = id + "_" + TimeUtil.formatDate(System.currentTimeMillis(), TimeFormat.YYYYMMDD);
    final Long times = (Long)userReplyTopicTimeCachePerDay.get(key);
    if (times != null){
      userReplyTopicTimeCachePerDay.put(key, times + 1L);
    }else {
      userReplyTopicTimeCachePerDay.put(key, 1L);
    }
  }
  
  /**
   * 判断回帖是否需要验证码
   * @param id sasUserId
   */
  public static final boolean replyTopicNeedCaptcha(final long id){
    if (id < 1){
      return false;
    }
    final String key = id + "_" + TimeUtil.formatDate(System.currentTimeMillis(), TimeFormat.YYYYMMDD);
    final Long times = (Long)userReplyTopicTimeCachePerDay.get(key);
    return times != null && times >= ForumConstant.ForumNeedCaptchaTimes.ReplyTopic.time;
  }
	/*****************
	 * 转成以板块id作为key的map， value为SasMenuBbsForum列表
	 * @param list
	 * @return
	 */
	public static final Map<Long, List<SasMenuBbsForum>> extractForums2CategoryKeyMap(final List<SasMenuBbsForum> list){
		final Map<Long, List<SasMenuBbsForum>> forumsByCategory = new HashMap<Long, List<SasMenuBbsForum>>();
		for(final SasMenuBbsForum forum: list){
			List<SasMenuBbsForum> forums = forumsByCategory.get(forum.getCategoryId());
			if(forums == null){
				forums = new LinkedList<SasMenuBbsForum>();
			}
			forums.add(forum);
			forumsByCategory.put(forum.getCategoryId(), forums);
		}
		return forumsByCategory;
	}
	
	/********************
	 * 转成以板块id作为key的map， value为SasMenuBbsForum.id列表
	 * @param list
	 * @return
	 */
	public static final Map<Long, List<Long>> extractForumIds2CategoryKeyMap(final List<SasMenuBbsForum> list)
	{
		final Map<Long, List<Long>> forumIdsByCategory = new HashMap<Long, List<Long>>();
		for(SasMenuBbsForum  forum: list){
			List<Long> forumIds = forumIdsByCategory.get(forum.getCategoryId());
			if(forumIds == null){
				forumIds = new LinkedList<Long>();
			}
			forumIds.add(forum.getId());
			forumIdsByCategory.put(forum.getCategoryId(), forumIds);
		}
		return forumIdsByCategory;
	}
	
	/*****************
	 * 获取idsString里面的前count的id字符串， 分隔符为divider
	 * @param idsString
	 * @param divider
	 * @param count
	 * @return
	 */
	public static final Long[] extractPreviousIds(final String idsString, final DividerChar dividerChar, final int count)
	{
		if(StringUtils.isBlank(idsString)){
			return new Long[0];
		}
		final String[] array = idsString.split(dividerChar.chars);
		final List<Long> list = new ArrayList<Long>(count);
		for(int i=0; i<array.length && list.size()<=count; i++){
			if(StringUtils.isBlank(array[i])){
				continue;
			}
			final long id = IdUtil.convertTolong(array[i].trim(), 0L);
			if(id > 0){
				list.add(id);
			}
		}
		return list.toArray(new Long[list.size()]);
	}
	
	/********************
	 * 解析最近回复的楼层信息
	 * @param lastReplyIds 最近回复的用户id列表
	 * @param lastReplyUserIds 最近回复的用户id列表
	 * @param lastReplyUserNicknames  最近回复的用户昵称列表
	 * @param lastReplyContents 最近回复的用户评论内容列表
	 * @param lastReplyTimes 最近回复的用户时间列表
	 * @return
	 */
	public static final List<SasMenuBbsForumReply> extractReplyList(final long sasId, final long menuId, 
			final long forumId, final long currentReplyId, final String lastReplyIds, 
			final String lastReplyUserIds, final String lastReplyUserNicknames, final String lastReplyContents, 
			final String lastReplyTimes, final String lastReplyUserAvatars, 
			final DividerChar dividerChar, final int limit, final EnvironmentService environmentService)
	{
		final String[] lastReplyIdArray = StringUtils.isBlank(lastReplyIds) ? null :lastReplyIds.split(dividerChar.chars);
		if(lastReplyIdArray == null || lastReplyIdArray.length < 1){
			return new ArrayList<SasMenuBbsForumReply>(0);
		}		
		//split有个bug，如果分割符后面没有字符，则不分割， 例如1;#;2;#;3;#;;#;分割后长度为3
		final String[] lastReplyUserIdArray = lastReplyUserIds.split(dividerChar.chars);
		final String[] lastReplyUserNicknameArray = (lastReplyUserNicknames+dividerChar.chars+" ").split(dividerChar.chars);
		final String[] lastReplyContentsArray = lastReplyContents.split(dividerChar.chars);
		final String[] lastReplyTimesArray = lastReplyTimes.split(dividerChar.chars);
		final String[] lastReplyUserAvatarsArray = (lastReplyUserAvatars+dividerChar.chars+" ").split(dividerChar.chars);
		final String domain = CDNMonitorDataCache.instance.getCssJSDomain(environmentService, null, null);
		final List<SasMenuBbsForumReply> result = new ArrayList<SasMenuBbsForumReply>(lastReplyIdArray.length);
		for(int i=0; i<lastReplyIdArray.length && i<lastReplyUserIdArray.length
				&& i<lastReplyUserNicknameArray.length && i<lastReplyContentsArray.length
				&& i<lastReplyTimesArray.length && i<lastReplyUserAvatarsArray.length
				&& i<limit; i++)
		{
			final SasMenuBbsForumReply reply = new SasMenuBbsForumReply();
			reply.setSasId(sasId);
			reply.setMenuId(menuId);
			reply.setForumId(forumId);
			reply.setId(IdUtil.convertTolong(lastReplyIdArray[i], 0L));
			reply.setUserId(IdUtil.convertTolong(lastReplyUserIdArray[i], 0L));
			reply.setUserNickname(lastReplyUserNicknameArray[i].trim());			
			reply.setContent(EmotionUtil.emotion2Img(domain,lastReplyContentsArray[i]));
			reply.setCreateTime(IdUtil.convertTolong(lastReplyTimesArray[i], 0L));
			reply.setUserAvatar(lastReplyUserAvatarsArray[i].trim());
			reply.setAncestorReplyFloorId(currentReplyId);
			result.add(reply);
		}		
		return result;
	}
	
	/********************
	 * 将数据插入到头部，不去重
	 * @param sourceString
	 * @param divider
	 * @param maxCount
	 * @return
	 */
	public static final String insertIdToHeader(String sourceString, final DividerChar dividerChar,
			final String newId, final int maxCount)
	{
		if(sourceString == null){
			return newId;
		}
		sourceString = newId + dividerChar.chars + sourceString;
		final String[] array = sourceString.split(dividerChar.chars);
		if(array.length <= maxCount){
			return sourceString;
		}
		final StringBuilder sb = new StringBuilder("");
		for(int i=0; i<maxCount&&i<array.length; i++){
			if(i > 0){
				sb.append(dividerChar.chars);
			}
			sb.append(array[i]);
		}
		return sb.toString();
	}
	
	/**********************
	 * 生成最近的点评回复人的信息
	 * @param parentReply
	 * @param childReplys
	 * @param divider
	 */
	public static final void updateLastChildrenReplys(final SasMenuBbsForumReply parentReply, 
			final List<SasMenuBbsForumReply> childReplys,
			final DividerChar dividerChar)
	{
		if(parentReply == null){
			return;
		}		
		final StringBuilder lastReplyUserIds = new StringBuilder("");/** 最近回复的用户id列表 **/		
		final StringBuilder lastReplyUserAvatars=new StringBuilder("");/** 最后回复的用户的头像**/		
		final StringBuilder lastReplyUserNicknames = new StringBuilder("");/** 最近回复的用户昵称列表 **/		
		final StringBuilder lastReplyContents = new StringBuilder("");/** 最近回复的用户评论内容列表 **/		
		final StringBuilder lastReplyTimes = new StringBuilder("");/** 最近回复的用户时间列表 **/		
		final StringBuilder lastReplyIds = new StringBuilder("");/** 最近回复的用户id列表 **/
		if(CollectionUtils.isNotEmpty(childReplys)){
			int index = 0;
			for(SasMenuBbsForumReply reply : childReplys){
				lastReplyUserIds.append((index < 1) ? reply.getUserId() : dividerChar.chars + reply.getUserId());
				lastReplyUserAvatars.append((index < 1) ? StringUtils.defaultIfBlank(reply.getUserAvatar(), UserConstant.UserDefaultAvatar)
						: dividerChar.chars + StringUtils.defaultIfBlank(reply.getUserAvatar(), UserConstant.UserDefaultAvatar));
				lastReplyUserNicknames.append((index < 1) ? StringUtils.defaultIfBlank(reply.getUserNickname(), "-")
						: dividerChar.chars + StringUtils.defaultIfBlank(reply.getUserNickname(), "-"));
				lastReplyContents.append((index < 1) ? StringUtils.defaultIfBlank(reply.getContent(), "-") 
						: dividerChar.chars + StringUtils.defaultIfBlank(reply.getContent(), "-"));
				lastReplyTimes.append((index < 1) ? reply.getCreateTime() : dividerChar.chars + reply.getCreateTime());		
				lastReplyIds.append((index < 1) ? reply.getId() : dividerChar.chars + reply.getId());
				index ++;
			}
		}		
		//更新本楼信息		
		parentReply.setLastReplyIds(lastReplyIds.toString());
		parentReply.setLastReplyTimes(lastReplyTimes.toString());
		parentReply.setLastReplyUserIds(lastReplyUserIds.toString());
		parentReply.setLastReplyUserNicknames(lastReplyUserNicknames.toString());
		parentReply.setLastReplyContents(lastReplyContents.toString());
		parentReply.setLastReplyUserAvatars(lastReplyUserAvatars.toString());
	}	
	
	/**********************
	 * 添加压缩样式
	 */
	public static final List<SasMenuBbsForumReply> addQiniuCompressStyle(final List<SasMenuBbsForumReply> replys,
			final QiNiuImageCompress compress){
		if(replys == null){
			return new ArrayList<SasMenuBbsForumReply>(0);
		}
		for(SasMenuBbsForumReply reply: replys){
			reply.setContent(HtmlUtil.processContent4Mobile(reply.getContent(), compress.style, false));
		}
		return replys;
	}
	
	/*******************
	 * 生成点评界面的用户评论摘要信息
	 * @param content
	 * @param maxLength
	 * @return
	 */
	public static final String generateReplySummary(final EnvironmentService environmentService, 
			String content, final int maxLength)
	{
		content = EmotionUtil.Img2Emotion(content);
		content = HtmlUtil.getPlainTextByReplaceImgNode(content, "【图片】", maxLength, true, false);
		String domain = CDNMonitorDataCache.instance.getCssJSDomain(environmentService, null, null);
		return EmotionUtil.emotion2Img(domain, content);
	}
	
	/************
	 * 一个帖子预览时的缓存key
	 * @param categoryId
	 * @param userId
	 * @return
	 */
	public static final String createForumDaoPreviewCacheKey(final long categoryId, final long userId){
		return "tb_sas_menu_bbs_forum_Preview_" + categoryId	+ "_" + userId;
	}
	
	/******************
	 * 确保每个版块的权限 不超过 父级菜单的权限
	 * @param setting
	 * @param list
	 */
	public static final List<SasMenuBbsForumCategory> settingAuthority(final SasMenuBbsSetting setting, final List<SasMenuBbsForumCategory> list)
	{
		if(list == null){
			return new ArrayList<SasMenuBbsForumCategory>(0);
		}
		//设置权限信息， 父级权限大于子级
		if(setting.getForumPubAuthority() == ActionAuthority.DisAllow.authority 
				|| setting.getForumReplyPubAuthority() == ActionAuthority.DisAllow.authority )
		{
			for(SasMenuBbsForumCategory sc : list){
				settingAuthority(setting, sc);
			}
		}
		return list;
	}
	
	/****************
	 * 确保一个板块的权限不超过整个菜单的权限
	 * @param setting
	 * @param category
	 * @return
	 */
	public static final SasMenuBbsForumCategory settingAuthority(final SasMenuBbsSetting setting, final SasMenuBbsForumCategory category)
	{
		if(setting.getForumPubAuthority() == ActionAuthority.DisAllow.authority){
			category.setForumPubAuthority(ActionAuthority.DisAllow.authority);
		}
		if(setting.getForumReplyPubAuthority() == ActionAuthority.DisAllow.authority){
			category.setForumReplyPubAuthority(ActionAuthority.DisAllow.authority);
		}
		return category;
	}
	
	/********************
	 * 创建帖子详情页的链接
	 * @param forumId
	 * @param page
	 * @param currentForumCreateTime
	 * @return
	 */
	public static final String createForumDetailPageURL(final long forumId, final int page, final long currentForumCreateTime){
		final String url = "/bbs/topic?id=" + forumId;
		if(page > 0){
			return url + "&page=" + page;
		}
		if(currentForumCreateTime >0){
			return url + "&time=" + currentForumCreateTime;
		}
		return url;
	}
	
	/********************
	 * 创建帖子作品详情页的链接
	 * @param forumVoteItemId
	 * @return
	 */
	public static final String createForumVoteItemDetailPageURL(final long forumVoteItemId){
		return "/bbs/topic/vote/item?id=" + forumVoteItemId;
	}
	
	/********************
	 * 创建帖子板块详情页的链接
	 * @param categoryId
	 * @return
	 */
	public static final String createForumCategoryDetailPageURL(final long categoryId,final boolean isMobile){
		if(isMobile){
			return "/m/bbs/category/detail?id=" + categoryId;
		}
		return "/bbs/category/detail?id=" + categoryId;
	}
	
	
	/*****************
	 * 将一批话题转成document列表
	 * @param list
	 * @return
	 */
	public static final List<Document> convert2Documents(final List<SasMenuBbsForum> list){
		if(CollectionUtils.isEmpty(list)){
			return new ArrayList<Document>(0);
		}
		final List<Document> result = new ArrayList<Document>(list.size());
		for(final SasMenuBbsForum forum : list){
			final Document doc = convert2Document(forum);
			if(doc != null){
				result.add(doc);
			}
		}		
		return result;
	}
	
	/*****************
	 * 将一个话题转成一个doc	
	 * @param forum
	 * @return
	 */
	public static final Document convert2Document(final SasMenuBbsForum forum){
		try{
			Document doc = new Document();
			doc.add(new StringField(FullTextField.Id.name, String.valueOf(forum.getId()), Field.Store.YES));//不分词的索引
			doc.add(new StringField(FullTextField.SasId.name, String.valueOf(forum.getSasId()), Field.Store.YES));//不分词的索引
			doc.add(new StringField(FullTextField.MenuId.name, String.valueOf(forum.getMenuId()), Field.Store.YES));//不分词的索引	
			doc.add(new StringField(FullTextField.UserId.name, String.valueOf(forum.getUserId()), Field.Store.YES));//不分词的索引	
			doc.add(new StringField("categoryId", String.valueOf(forum.getCategoryId()), Field.Store.YES));//不分词的索引
			doc.add(new IntField("topState", forum.getTopState(), Field.Store.YES));
			doc.add(new IntField("goodState", forum.getGoodState(), Field.Store.YES));
			doc.add(new LongField(FullTextField.UpdateTime.name, forum.getLastUpdateTime(), Field.Store.YES));
			doc.add(new LongField(FullTextField.CreateTime.name, forum.getCreateTime(), Field.Store.YES));
			doc.add(new TextField(FullTextField.SearchData.name,
					LuceneAnalyzerUtil.instance.splitContents2JoinWhiteSpace(forum.getTitle()), Field.Store.NO));
			return doc;
		}catch(Exception ex){
			logger.error("Fail to createDocument, ex="+ex.getMessage() + ", act=" + ReflectionToStringBuilder.toString(forum), ex);
			return null;
		}
	}
	
	/*****************
	 * 判断是否是bbs类型的菜单
	 * @param menuTypeCode
	 * @return
	 */
	public static final boolean isBBSMenu(final String menuTypeCode){
		return isBBSMenu(MenuType.parse(menuTypeCode));
	}	
	
	public static final boolean isBBSMenu(final MenuType type){
		return MenuType.MultiBBS == type
				|| MenuType.SingleBBS == type ;
	}	
	
	/******************
	 * 对板块标题进行html encode
	 * @param list
	 */
	public static final void encodeHTMLTitles(final List<SasMenuBbsForumCategory> list)
	{
		if(CollectionUtils.isNotEmpty(list)){
			for(final SasMenuBbsForumCategory c : list){
				c.setName(XSSUtil.encode(c.getName()));
			}
		}
	}
	
	/******************
	 * 创建回复别人的回复时， 本条回复的quote内容
	 * @param reply
	 * @return
	 */
	public static final Map<String,Object> createParentReplyQuote(final EnvironmentService environmentService, final SasMenuBbsForumReply reply)
	{
		if(reply == null){
			return new HashMap<String,Object>();
		}
		//替换图片
		final String domain = CDNMonitorDataCache.instance.getCssJSDomain(environmentService, null, null);
		String replyContent = HtmlUtil.getPlainTextByReplaceImgNode(reply.getContent(), "", 
				SasTrackConstants.TrackCommentContentStoreMaxLength, false, false); //已经转换了， 故false即可
		replyContent = StringUtils.isBlank(replyContent) ? "" : EmotionUtil.emotion2Img(domain, replyContent);		
		//保存内容
		final Map<String,Object> map = new HashMap<String,Object>();
		map.put("content", replyContent);
		map.put("nickname", reply.getUserNickname());
		map.put("avatar", reply.getUserAvatar());
		map.put("userId", reply.getUserId());
		map.put("createTime", reply.getCreateTime());
		return map;
	}
	
	/*****************
	 * 删除内容中的富文本内容， 生成摘要
	 * @param activities
	 * @param maxLetterCount：保留的最多字符个数， 全部保留传入-1
	 */
	public static final List<SasMenuBbsForumReply> removeHTMLFromContent(final EnvironmentService environmentService, 
			final List<SasMenuBbsForumReply> replys, 
			final int maxLetterCount, final boolean needKeepEmotion){
		if(replys == null){
			return null;
		}
		final String domain = CDNMonitorDataCache.instance.getCssJSDomain(environmentService, null, null);
		for(final SasMenuBbsForumReply reply : replys){
			String replyContent = HtmlUtil.getPlainTextByReplaceImgNode(reply.getContent(), "", 
					maxLetterCount, false, false);
			if(needKeepEmotion){
				replyContent = StringUtils.isBlank(replyContent) ? "" : EmotionUtil.emotion2Img(domain, replyContent);	
			}
			reply.setContent(replyContent);			
		}
		return replys;
	}
	
	public static final SasMenuBbsForumReply removeHTMLAndEmotionFromContent(final SasMenuBbsForum forum,
			final SasMenuBbsForumReply reply,  final int maxLetterCount){
		if(reply == null){
			return null;
		}
		String replyContent = null;
		if(forum.getSummary() != null && forum.getSummary().length() > 0){
			replyContent = HtmlUtil.subStringWithEnglish2Letters(forum.getSummary(), maxLetterCount).key;
		}else{
			replyContent = HtmlUtil.getPlainTextByReplaceImgNode(reply.getContent(), "", 
				maxLetterCount, false, false);
			forum.setSummary(replyContent);
		}
		reply.setContent(replyContent);
		return reply;
	}
	
	/*************
	 * 是否允许在该板块下面发帖
	 * @param setting
	 * @param category
	 * @return
	 */
	public static final boolean isAllowPublishForum(final SasMenuBbsSetting setting, final SasMenuBbsForumCategory category){
		return setting != null && category != null 
				&& setting.getForumPubAuthority() == ActionAuthority.Allow.authority
				&& category.getForumPubAuthority() == ActionAuthority.Allow.authority;
	}
	
	/***************
	 * 是否允许发布帖子回复
	 * @param setting
	 * @param category
	 * @return
	 */
	public static final boolean isAllowPublishForumReply(final SasMenuBbsSetting setting, final SasMenuBbsForumCategory category){
		return setting != null && category != null 
				&& setting.getForumReplyPubAuthority() == ActionAuthority.Allow.authority
				&& category.getForumReplyPubAuthority() == ActionAuthority.Allow.authority;
	}
	
	/**
	 * 剔除不允许发帖的板块
	 * @param setting
	 * @param sourceCategoryList
	 * @return
	 */
	public static final List<SasMenuBbsForumCategory> rejectNotAllowedCategory(
			final SasMenuBbsSetting setting, final List<SasMenuBbsForumCategory> sourceCategoryList){
		List<SasMenuBbsForumCategory> resultCategoryList = new ArrayList<SasMenuBbsForumCategory>(sourceCategoryList.size());
		for(SasMenuBbsForumCategory sourceCategory : sourceCategoryList){
			if(ForumUtil.isAllowPublishForum(setting, sourceCategory)){
				resultCategoryList.add(sourceCategory);
			}
		}
		return resultCategoryList;
	}
	
	/****************
	 * 转成配置信息对应的MAP
	 * @param extConfig
	 * @return
	 */
	public static final Map<String, String> convert2ExtConfig(final String extConfig, final char forumType)
	{
		Map<String, String> map = null;
		if(StringUtils.isNotBlank(extConfig)){
			map = (Map<String, String>)JsonUtil.getObject(extConfig, Map.class);
		}
		if(map == null){
			map = new HashMap<String, String>();
		}
		for(ForumExtConfig field : ForumExtConfig.allVoteConfigs())
		{
			if(!map.containsKey(field.fieldName)){
				map.put(field.fieldName, field.defaultValue);
			}
		}
		//容错，之前作品型和图片型允许多选， 将来只允许单选， 但可以投多次
		if(ForumType.AdvanceVote.type == forumType || ForumType.PictureVote.type == forumType){
			//不再支持一次多选
			final String value = map.get(ForumExtConfig.VoteOptionSelectCount.fieldName);
			if(!ForumExtConfig.VoteOptionSelectCount.defaultValue.equals(value)){
				map.put(ForumExtConfig.VoteOptionSelectCount.fieldName, ForumExtConfig.VoteOptionSelectCount.defaultValue);
				if(IdUtil.convertToInteger(value, 1) > 1){
					map.put(ForumExtConfig.UserVoteLimit.fieldName, value);
				}
			}
		}
		return map;
	}
	
	/************
	 * 转成MAP
	 * @param voteSelect
	 * @param voteResultShow
	 * @param voteStartTime
	 * @param voteEndTime
	 * @return
	 */
	public static final String convert2ExtConfigString(final int voteOptionSelectCountLimit, //是否单选
			final BinaryState voteResultShow, //是否可查看结果
			final BinaryState voteUserUpload, //是否用户可以上传作品
			final BinaryState videoIsRequired, //是否视频必须
			final int userVoteLimit, //每个用户允许的投票次数，0表示每个用户只允许一次，其他数字表示每个帐号每天最多能投n次
			final long voteStartTime, //投票开始时间
			final long voteEndTime)
	{
		final Map<String, String> map = new HashMap<String, String>();
		map.put(ForumExtConfig.VoteOptionSelectCount.fieldName, String.valueOf(voteOptionSelectCountLimit < 1 ? 1 : voteOptionSelectCountLimit));
		map.put(ForumExtConfig.VoteResultShowAll.fieldName, String.valueOf(voteResultShow.state));
		map.put(ForumExtConfig.VoteCanUserUpload.fieldName, String.valueOf(voteUserUpload.state));
		map.put(ForumExtConfig.VideoIsRequired.fieldName, voteUserUpload == BinaryState.No ? String.valueOf(BinaryState.No.state)
				: String.valueOf(videoIsRequired.state));
		map.put(ForumExtConfig.UserVoteLimit.fieldName, String.valueOf(userVoteLimit < 0 ? 0 : userVoteLimit));
		map.put(ForumExtConfig.VoteTime.fieldName, String.valueOf(voteStartTime + DividerChar.SingleWells.chars + voteEndTime));
		return JsonUtil.getJsonString(map);
	}
	
	/*************
	 * 解析比赛时间
	 * @param timeString
	 * @return
	 */
	public static final BinaryEntry<Long, Long> parseForumVoteTime(final String timeString){
		if(timeString != null && timeString.contains(DividerChar.SingleWells.chars)){
			final String[] times = timeString.split(DividerChar.SingleWells.chars);
			return new BinaryEntry<Long, Long>(IdUtil.convertTolong(times[0], 0), IdUtil.convertTolong(times[1], 0));
		}else{
			return new BinaryEntry<Long, Long>(0L, 0L);
		}
	}
	
	/*****************
	 * 插入最近对作品投票的人
	 * @param statistic
	 * @param user
	 * @return
	 */
	public static final String insertOneNewUserVote(final String oldLastVoters, final DividerChar dividerChar, final User user, int maxCount)
	{
		final String userIdString = String.valueOf(user.getId());
		if(StringUtils.isBlank(oldLastVoters)){
			return userIdString;
		}
		//老用户数据
		final String[] visitorIdArray = oldLastVoters.split(dividerChar.chars);
		if(ArrayUtils.isEmpty(visitorIdArray) || visitorIdArray[0].length() < 1){
			return userIdString;
		}else{
			//拼接信息
			final StringBuilder allLastVoterIds = new StringBuilder(userIdString);
			maxCount = MathUtil.minInt(visitorIdArray.length, maxCount);
			//去重
			final Set<String> uniqueUserIdSet = new HashSet<String>();
			uniqueUserIdSet.add(userIdString);
			for(int i=0; i<maxCount; i++)
			{
				if(uniqueUserIdSet.contains(visitorIdArray[i])){
					continue;
				}
				uniqueUserIdSet.add(visitorIdArray[i]);
				allLastVoterIds.append(dividerChar.chars + visitorIdArray[i]);
			}
			return allLastVoterIds.toString();
		}
	}
	
	/**************
	 * 生成作品排序值， 投票多的考前， 同样投票的， 则越老的越在前
	 * @param totalVoteCount
	 * @param entryId
	 * @return
	 */
	public static final long generateForumVoteItemSort(final long totalVoteCount, final long voteItemId){
		long IdBalance = 9999999999L - voteItemId; //保证老的在前面
		final String v = String.valueOf(totalVoteCount) + String.valueOf(IdBalance);
		return Long.parseLong(v);
	}
	
	/*****************
	 * 计算问卷文字型投票的bar显示条的总长度的比例， 防止轻易按投票数变动
	 * @param totalVoteCount
	 * @return
	 */
	public static final float calculateDigitalVoteBarTotalLength(final long totalVoteCount){
		if(totalVoteCount < 10){
			return 0.2f;
		}else if(totalVoteCount < 50){
			return 0.4f;
		}else if(totalVoteCount < 100){
			return 0.6f;
		}else if(totalVoteCount < 500){
			return 0.8f;
		}else {
			return 1.0f;
		} 
	}
	
	/*************
	 * 将附件转成字符串
	 * @param list
	 * @return
	 */
	public static final String convert2Attachments(final List<AttachmentFile> list)
	{
		final List<Map<String, Object>> data = new LinkedList<Map<String, Object>>();
		if(CollectionUtils.isNotEmpty(list))
		{
			for(final AttachmentFile a : list){
				final Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", a.getName());
				map.put("url", a.getUrl());
				map.put("size", a.getSize());
				data.add(map);
			}
		}
		return JsonUtil.getJsonString(data); 
	}
	
	/************
	 * 从字符串转出附件信息
	 * @param v
	 * @return
	 */
	public static final List<AttachmentFile> convertFromAttachments(final String v)
	{
		if(StringUtils.isBlank(v)){
			return new ArrayList<AttachmentFile>(0);
		}
		try{
			final List<AttachmentFile> list = new LinkedList<AttachmentFile>();
			final List<Map<String, Object>> data = JsonUtil.getObject(v, List.class);
			for(final Map<String, Object> map : data)
			{
				final AttachmentFile f = new AttachmentFile();
				f.setName(String.valueOf(map.get("name")));
				f.setSize(IdUtil.convertTolong(map.get("size"), 0));
				f.setUrl(String.valueOf(map.get("url")));
				list.add(f);
			}
			return list;
		}catch(Exception ex){
			logger.error("fail to convertFromAttachments:" + v, ex);
			return new ArrayList<AttachmentFile>(0);
		}
	}
	
	/*************
	 * 删除不再使用的附件
	 * @param newAttachments
	 * @param oldAttachments
	 */
	public static final void deleteUnusedAttachmentsByAsynchronize(final EnvironmentService environmentService, 
			final List<AttachmentFile> newAttachments, final List<AttachmentFile> oldAttachments)
	{
		if(CollectionUtils.isEmpty(oldAttachments) || environmentService.getEnvironmentType() != EnvironmentType.Online){
			return;
		}
		final Set<String> usingAttachments = new HashSet<String>();
		if(CollectionUtils.isNotEmpty(newAttachments))
		{
			for(final AttachmentFile a : newAttachments)
			{
				usingAttachments.add(a.getUrl());
			}
		}
		for(final AttachmentFile a : oldAttachments)
		{
			if(!usingAttachments.contains(a.getUrl()))
			{
				QiNiuUtil.deleteAttachmentQiniuResourceByAsynchronize(environmentService, QiNiuSpace.AllAttachement, a.getUrl(), 5);
			}
		}
	}

}