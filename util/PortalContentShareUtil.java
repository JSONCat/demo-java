/**
 * 
 */
package com.sas.core.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import com.sas.core.constant.CommonConstant.RecommendState;
import com.sas.core.constant.ForumConstant.ForumGoodState;
import com.sas.core.constant.FullTextSearchConstant.FullTextField;
import com.sas.core.constant.PortalConstant.SasContentType;
import com.sas.core.dto.PortalContentShareAlbum;
import com.sas.core.dto.PortalContentShareArticle;
import com.sas.core.dto.PortalContentShareDTO;
import com.sas.core.dto.SasMenuAlbumDetail;
import com.sas.core.dto.SimplePortalContentShareDTO;
import com.sas.core.dto.WapPortalContentShareDTO;
import com.sas.core.dto.WapPortalFavorityDTO;
import com.sas.core.dto.WebPortalFavorityShareDTO;
import com.sas.core.dto.sharecontemt.ArticleContentShare;
import com.sas.core.dto.sharecontemt.BBSContentShare;
import com.sas.core.dto.sharecontemt.DestinationContentShare;
import com.sas.core.meta.PortalContentShare;
import com.sas.core.meta.SasMenuAlbum;
import com.sas.core.meta.SasMenuArticle;
import com.sas.core.meta.SasMenuArticleStatistic;
import com.sas.core.meta.SasMenuBbsForum;
import com.sas.core.meta.SasMenuBbsForumStatistic;

/**
 * @author Administrator
 *
 */
public class PortalContentShareUtil {

	private static final Logger logger = Logger.getLogger(PortalContentShareUtil.class);
	
	/****************
	 * 以类型为key， 进行转换
	 * @param shareList
	 * @return
	 */
	public static final Map<SasContentType, List<PortalContentShare>> mapByContentType(final List<PortalContentShare> shareList)
	{
		if(CollectionUtils.isEmpty(shareList)){
			return new HashMap<SasContentType, List<PortalContentShare>>();
		}
		final Map<SasContentType, List<PortalContentShare>> map = new HashMap<SasContentType, List<PortalContentShare>>();
		for(final PortalContentShare share : shareList){
			final SasContentType type = SasContentType.parse(share.getContentType());
			List<PortalContentShare> list = map.get(type);
			if(list == null){
				list = new LinkedList<PortalContentShare>();
			}
			list.add(share);
			map.put(type, list);
		}
		return map;
	}
	
	/***************
	 * convert to map
	 * @param shareList
	 * @return
	 */
	public static final Map<String, PortalContentShare> mapByContentTypeStringKey(final List<PortalContentShare> shares)
	{
		final Map<String, PortalContentShare> shareMap = new HashMap<String, PortalContentShare>();
		if(CollectionUtils.isNotEmpty(shares)){
			for(final PortalContentShare share : shares){
				shareMap.put(SasContentType.parse(share.getContentType()).createCacheKey(share.getContentId()), share);
			}
		}
		return shareMap;
	}

	/****************
	 * 以类型为key， 进行转换
	 * @param shareList
	 * @return
	 */
	public static final Map<SasContentType, List<Long>> mapContentIdByContentType(final List<PortalContentShare> shareList)
	{
		if(CollectionUtils.isEmpty(shareList)){
			return new HashMap<SasContentType, List<Long>>();
		}
		final Map<SasContentType, List<Long>> map = new HashMap<SasContentType, List<Long>>();
		for(final PortalContentShare share : shareList){
			final SasContentType type = SasContentType.parse(share.getContentType());
			List<Long> list = map.get(type);
			if(list == null){
				list = new LinkedList<Long>();
			}
			list.add(share.getContentId());
			map.put(type, list);
		}
		return map;
	}
	
	/*******************
	 * 计算社区分享的排序值
	 * @param favourityCount
	 * @param visitCount
	 * @param recommendState
	 * @return
	 */
	public static final long calculateSort(final long favourityCount, final SasMenuArticle article, final SasMenuArticleStatistic articleStatistic)
	{
		return favourityCount * 50 + (articleStatistic == null ? 0 : articleStatistic.getTotalViewCount())
				+ (article.getRecommendState() == RecommendState.Recommend.state ? 10 : 0);
	}
	
	public static final long calculateSort(final long favourityCount, final SasMenuAlbum album)
	{
		return favourityCount * 50 + album.getTotalPhotoCount()
				+ (album.getTotalPhotoRecommendCount() > 0 ? 10 : 0);
	}
	
	public static final long calculateSort(final long favourityCount, final SasMenuBbsForum forum, final SasMenuBbsForumStatistic forumStatistic)
	{
		return favourityCount * 50 + (forumStatistic == null ? 0 :forumStatistic.getTotalViewCount())
				+ (forum.getGoodState() == ForumGoodState.Good.state ? 10 : 0);
	}
	
	
	
	/*****************
	 * 将一个社区内容转成一个doc	
	 * @param act
	 * @return
	 */
	public static final Document convert2Document(final PortalContentShare share)
	{
		try{
			Document doc = new Document();
			doc.add(new StringField(FullTextField.Id.name, String.valueOf(share.getId()), Field.Store.YES));//不分词的索引	
			doc.add(new StringField(FullTextField.SasId.name, String.valueOf(share.getSasId()), Field.Store.YES));//不分词的索引
			doc.add(new StringField("contentId", String.valueOf(share.getContentId()), Field.Store.YES));//不分词的索引
			doc.add(new StringField("contentType", String.valueOf(share.getContentType()), Field.Store.YES));//不分词的索引
			doc.add(new StringField("articleContentType", String.valueOf(share.getArticleContentType()), Field.Store.YES));//不分词的索引
			doc.add(new TextField(FullTextField.SearchData.name, 
					LuceneAnalyzerUtil.instance.splitContents2JoinWhiteSpace(share.getContentTitle())
					, Field.Store.NO));
			doc.add(new LongField("sort", share.getSort(), Field.Store.YES));
			doc.add(new LongField(FullTextField.CreateTime.name, share.getCreateTime(), Field.Store.YES));
			return doc;
		}catch(Exception ex){
			logger.error("Fail to createDocument, ex="+ex.getMessage() + ", act=" + ReflectionToStringBuilder.toString(share), ex);
			return null;
		}
	}
	
	/****************
	 * 创建详情页链接
	 * @param share
	 * @return
	 */
	public static final String createDetailURL(final PortalContentShare share, final boolean isWap){
		if(share.getContentType() == SasContentType.AlbumPhoto.type){
			return (isWap ? "/m" : "") + "/album?id=" + share.getId();
		}else{					
			return (isWap ? "/m" : "") + "/article?id=" + share.getId() + "&contentType=" + share.getContentType();
		}
	}
	
	public static final String createDetailURLByContentId(final SasContentType contentType, final long contentId, final boolean isWap){
		return createDetailURLByContentId(contentType.type, contentId, isWap);
	}
	
	public static final String createDetailURLByContentId(final char contentType, final long contentId, final boolean isWap){
		if(contentType == SasContentType.AlbumPhoto.type){
			return (isWap ? "/m" : "") + "/album?contentId=" + contentId;
		}else{					
			return (isWap ? "/m" : "") + "/article?contentId=" + contentId+ "&contentType=" + contentType;
		}
	}
	
	/***************
	 * 添加虚假计数
	 * @param share
	 */
	public static final PortalContentShareArticle addVirtualVisitAndFavourityStatisticCount(final PortalContentShareArticle share){
		if(share != null){
			final long contentId = share.getShare().getContentId();
			if(share.getShare().getContentType() != SasContentType.AlbumPhoto.type){
				share.setTotalViewCount(createVirtualVisitCount(contentId, share.getTotalViewCount()));
			}
			share.getShare().setFavorityCount(createVirtualFavourityCount(contentId, share.getShare().getFavorityCount()));
		}
		return share;
	}
	
	public static final long createVirtualFavourityCount(final long contentId, final long oldCount){
		return contentId%5 + oldCount;
	}
	
	public static final long createVirtualVisitCount(final long contentId, final long oldCount){
		return contentId%331 + contentId%39 + oldCount;
	}
	
	/***************
	 * 添加虚假计数
	 * @param share
	 */
	public static final PortalContentShareAlbum addVirtualVisitAndFavourityStatisticCount(final PortalContentShareAlbum share){
		if(share != null){
			share.getShare().setFavorityCount(createVirtualFavourityCount(share.getShare().getContentId(), share.getShare().getFavorityCount()));
		}
		return share;
	}
	
	/***************
	 * 添加虚假计数
	 * @param shares
	 */
	public static final SimplePortalContentShareDTO addVirtualVisitAndFavourityStatisticCount(final SimplePortalContentShareDTO dto){
		if(dto != null){
			final long contentId = dto.getContentId();
			if(dto.getContentType() != SasContentType.AlbumPhoto.type){
				dto.setVisitCount(createVirtualVisitCount(contentId, dto.getVisitCount()));
			}
			dto.setFavoriteCount(createVirtualFavourityCount(contentId, dto.getFavoriteCount()));
		}
		return dto;
	}
	
	public static final WapPortalContentShareDTO addVirtualVisitAndFavourityStatisticCount(final WapPortalContentShareDTO dto){
		if(dto != null){
			final long contentId = dto.getShare().getContentId();
			if(dto.getShare().getContentType() != SasContentType.AlbumPhoto.type){
				dto.setTotalViewCount(createVirtualVisitCount(contentId, dto.getTotalViewCount()));
			}
			dto.getShare().setFavorityCount(createVirtualFavourityCount(contentId, dto.getShare().getFavorityCount()));
		}
		return dto;
	}
	
	public static final WapPortalFavorityDTO addVirtualVisitStatisticCount(final WapPortalFavorityDTO dto){
		if(dto != null){
			final long contentId = dto.getContentId();
			if(dto.getContentType() != SasContentType.AlbumPhoto.type){
				dto.setTotalViewCount(createVirtualVisitCount(contentId, dto.getTotalViewCount()));
			}
		}
		return dto;
	}	
	
	public static final WebPortalFavorityShareDTO addVirtualVisitStatisticCount(final WebPortalFavorityShareDTO dto){
		if(dto != null){
			final long contentId = dto.getContentId();
			if(dto.getContentType() != SasContentType.AlbumPhoto.type){
				dto.setTotalViewCount(createVirtualVisitCount(contentId, dto.getTotalViewCount()));
			}
		}
		return dto;
	}	
	/**************
	 * 添加虚假计数 
	 * @param dto
	 * @return
	 */
	public static final PortalContentShareDTO<BBSContentShare> addBBSVirtualStatisticCount(final PortalContentShareDTO<BBSContentShare> dto)
	{
		if(dto != null){
			dto.setFavoriteCount(createVirtualFavourityCount(dto.getContentId(), dto.getFavoriteCount()));
			if(dto.getExtentContent() != null){
				final BBSContentShare forum = dto.getExtentContent();
				forum.getStatistic().setTotalViewCount((int)createVirtualVisitCount(dto.getContentId(), forum.getStatistic().getTotalViewCount()));
			}
		}
		return dto;
	}
	
	public static final PortalContentShareDTO<ArticleContentShare> addArticleVirtualStatisticCount(final PortalContentShareDTO<ArticleContentShare> dto)
	{
		if(dto != null){
			dto.setFavoriteCount(createVirtualFavourityCount(dto.getContentId(), dto.getFavoriteCount()));
			if(dto.getExtentContent() != null){
				final ArticleContentShare article = (ArticleContentShare)dto.getExtentContent();
				article.getStatistic().setTotalViewCount((int)createVirtualVisitCount(dto.getContentId(), article.getStatistic().getTotalViewCount()));
			}
		}
		return dto;
	}
	
	public static final PortalContentShareDTO<SasMenuAlbumDetail> addAlbumVirtualStatisticCount(final PortalContentShareDTO<SasMenuAlbumDetail> dto)
	{
		if(dto != null){
			dto.setFavoriteCount(createVirtualFavourityCount(dto.getContentId(), dto.getFavoriteCount()));
		}
		return dto;
	}
	
	public static final PortalContentShareDTO<DestinationContentShare> addDestinationVirtualStatisticCount(final PortalContentShareDTO<DestinationContentShare> dto)
	{
		if(dto != null){
			dto.setFavoriteCount(createVirtualFavourityCount(dto.getContentId(), dto.getFavoriteCount()));
		}
		return dto;
	}
	
	
	
}
