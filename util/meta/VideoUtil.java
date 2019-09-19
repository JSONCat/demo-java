/**
 * 
 */
package com.sas.core.util.meta;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.FullTextSearchConstant.FullTextField;
import com.sas.core.constant.ThirdPartConstant.QiNiuImageCompress;
import com.sas.core.constant.ThirdPartConstant.QiNiuSpace;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.dto.VideoMeta;
import com.sas.core.meta.SasMenuVideo;
import com.sas.core.meta.SasMenuVideoComment;
import com.sas.core.meta.SasVideo;
import com.sas.core.util.CollectionUtils;
import com.sas.core.util.EmotionUtil;
import com.sas.core.util.HtmlUtil;
import com.sas.core.util.IOUtil;
import com.sas.core.util.IdUtil;
import com.sas.core.util.LuceneAnalyzerUtil;

/**
 * 视频utility
 * @author Administrator
 *
 */
public final class VideoUtil {
	
	private static final Logger logger = Logger.getLogger(VideoUtil.class);

	/*****************
	 * 是否是mp4
	 * @param url
	 * @return
	 */
	public static final boolean isMP4(final String url)
	{
		return url != null && url.toLowerCase().endsWith(".mp4");
	}
	
	public static final boolean isMP3(final String url)
	{
		return url != null && url.toLowerCase().endsWith(".mp3");
	}
	
	/***********************
	 * 
	 * @param videoId
	 * @return
	 */
	public static final String createVideoDetailPageURL(final long videoId)
	{
		return "/video?id=" + videoId;
	}
	
	public static final String createVideoDetailPageURLUsingPathVariable(final long videoId)
	{
		return "/video/" + videoId;
	}
	
	/*****************
	 * 将一批视频 转成document列表
	 * @param list
	 * @return
	 */
	public static final List<Document> convert2Documents(final List<SasMenuVideo> list){
		if(CollectionUtils.isEmpty(list)){
			return new ArrayList<Document>(0);
		}
		final List<Document> result = new ArrayList<Document>(list.size());
		for(final SasMenuVideo video : list){
			final Document doc = convert2Document(video);
			if(doc != null){
				result.add(doc);
			}
		}		
		return result;
	}
	
	/*****************
	 * 将一个视频转成一个doc	
	 * @param Video
	 * @return
	 */
	public static final Document convert2Document(final SasMenuVideo video){
		try{
			Document doc = new Document();
			doc.add(new StringField(FullTextField.Id.name, String.valueOf(video.getId()), Field.Store.YES));//不分词的索引
			doc.add(new StringField(FullTextField.SasId.name, String.valueOf(video.getSasId()), Field.Store.YES));//不分词的索引
			doc.add(new StringField(FullTextField.MenuId.name, String.valueOf(video.getMenuId()), Field.Store.YES));//不分词的索引	
			doc.add(new StringField(FullTextField.UserId.name, String.valueOf(video.getUserId()), Field.Store.YES));//不分词的索引				
			doc.add(new LongField(FullTextField.UpdateTime.name, video.getLastUpdateTime(), Field.Store.YES));
			doc.add(new LongField(FullTextField.CreateTime.name, video.getCreateTime(), Field.Store.YES));
			doc.add(new TextField(FullTextField.SearchData.name, 
					LuceneAnalyzerUtil.instance.splitContents2JoinWhiteSpace(video.getTitle()), Field.Store.NO));
			return doc;
		}catch(Exception ex){
			logger.error("Fail to createDocument, ex="+ex.getMessage() + ", video=" + ReflectionToStringBuilder.toString(video), ex);
			return null;
		}
	}
	
	/****************
	 * 对评论添加压缩样式， 同时转化表情
	 * @param comments
	 * @param emotionDomain
	 * @param compreeStyle
	 * @return
	 */
	public static final List<SasMenuVideoComment> addEmotionAndImageCompressStyle(final List<SasMenuVideoComment> comments,
			final String emotionDomain, final QiNiuImageCompress compressStyle)
	{
		if(CollectionUtils.isEmpty(comments)){
			return comments;
		}
		for(final SasMenuVideoComment comment : comments){
			comment.setContent(EmotionUtil.emotion2Img(emotionDomain, comment.getContent()));
			comment.setContent(HtmlUtil.processContent4Mobile(comment.getContent(), compressStyle.style, false));
		}
		return comments;
	}
	
	/************
	 * 是否是视频的封面截图
	 * @param url
	 * @return
	 */
	public static final boolean isCoverGenerateByVideo(final String coverUrl){
		return coverUrl != null && (coverUrl.contains(QiNiuSpace.SaihuitongVideo.domain)
				|| coverUrl.contains(QiNiuSpace.WuFanVideo.domain));
	}
	
	/*********
	 * 是否是赛会通的视频
	 * @param url
	 * @return
	 */
	public static final boolean isSaihuitongVideo(final String url){
		return url != null && (url.contains(QiNiuSpace.SaihuitongVideo.domain)
				|| url.contains(QiNiuSpace.WuFanVideo.domain));
	}
	
	/*****************
	 * 分析url， 确认必要情况下产生MP4的链接
	 * @param videoURL
	 * @param uploadTime
	 * @return
	 */
	public static final String checkMP4Url(final String videoURL, final long uploadTime) {
		if(videoURL == null || videoURL.length() < 1){
			return "";
		}
		if(!videoURL.toLowerCase().endsWith(".mp4") && (System.currentTimeMillis() -
				uploadTime >= Miliseconds.ThirtySeconds.miliseconds)){
			return videoURL + ".mp4";
		}
		return videoURL;
	}
	
	public static final String checkMP4Url(final String videoURL) {
		if(videoURL == null || videoURL.length() < 1){
			return "";
		}
		if(!videoURL.toLowerCase().endsWith(".mp4")){
			return videoURL + ".mp4";
		}
		return videoURL;
	}
	
	/*************
	 * 抓取视频源文件信息
	 * @param url
	 * @return
	 */
	public static final VideoMeta fetchVideoMeta(final String url)
	{
		try{
			final HttpURLConnection httpUrl = (HttpURLConnection) new URL(url + "?avinfo").openConnection();  
			httpUrl.connect();
			final InputStream in = httpUrl.getInputStream();
			final String content = IOUtil.readTextFileUnderInputStream(in, Encoding.UTF8, "\n");
			if(content != null && content.length() > 10)
			{
				final VideoMeta meta = new VideoMeta();
				final String[] array = content.split("\\{|\\[|,|\\}|\\]");
				for(final String e : array)
				{
					if(e.length() < 1){
						continue;
					}
					final String[] subArray = e.replaceAll("[^a-zA-Z0-9:=]", "").split(":|=");
					if(subArray.length < 2){
						continue;
					}
					final String field = subArray[0].toLowerCase();
					if("size".equalsIgnoreCase(field)){
						final long s = IdUtil.convertTolong(subArray[1], 0L);
						if(s > 0){
							meta.setSize(s);//字节数
						}
					}else if("duration".equalsIgnoreCase(field)){
						final long s = IdUtil.convertTolong(subArray[1], 0L);
						if(s >= 1000000){
							meta.setDuration((int)(s/1000000) + 1);
						}								
					}else if("width".equalsIgnoreCase(field)){
						final int s = IdUtil.convertToInteger(subArray[1], 0);
						if(s > 0){
							meta.setWidth(s);
						}								
					}else if("height".equalsIgnoreCase(field)){
						final int s = IdUtil.convertToInteger(subArray[1], 0);
						if(s > 0){
							meta.setHeight(s);
						}
					}
				}
				IOUtil.closeStreamWithoutException(httpUrl);
				return meta;
			}
		}catch(Exception ex){
			logger.error("Fail to fetchVideoMeta, url=" + url, ex);
		}
		return null;
	}
	
	/***************
	 * 获取可以生成封面的url， 如果没有的话则返回null
	 * @param v
	 * @return
	 */
	public static final String generateAvailableCoverURL(final SasVideo v)
	{
		String cover = v.getUrl() + "?vframe/jpg/offset/2/w/980";
		if(IOUtil.isResourceAvaiable(cover)){
			return cover;
		}
		if(!VideoUtil.isMP4(v.getUrl())){
			cover = VideoUtil.checkMP4Url(v.getUrl()) + "?vframe/jpg/offset/2/w/980";
			if(IOUtil.isResourceAvaiable(cover)){
				return cover;
			}
			cover = VideoUtil.checkMP4Url(v.getUrl()) + "?vframe/jpg/offset/1/w/980";
			if(IOUtil.isResourceAvaiable(cover)){
				return cover;
			}
		}
		cover = v.getUrl() + "?vframe/jpg/offset/1/w/980";
		if(IOUtil.isResourceAvaiable(cover)){
			return cover;
		}
		return null;
	}
	
	/***********
	 * 能否上传视频
	 * @param uploadableBytes
	 * @return
	 */
	public static final boolean hasFreeStorage2Upload(final long uploadableBytes, final long fileSize){
		if(fileSize > 0){
			return uploadableBytes > fileSize;
		}else{
			return uploadableBytes > 1048576;
		}
	}
}
