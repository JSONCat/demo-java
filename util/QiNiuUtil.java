package com.sas.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.itextpdf.xmp.impl.Base64;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.rs.PutPolicy;
import com.sas.core.constant.CommonConstant.EnvironmentType;
import com.sas.core.constant.QRConstants.QRType;
import com.sas.core.constant.ThirdPartConstant;
import com.sas.core.constant.ThirdPartConstant.FileType;
import com.sas.core.constant.ThirdPartConstant.QiNiuImageCompress;
import com.sas.core.constant.ThirdPartConstant.QiNiuSpace;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.exception.ImageUploadException;
import com.sas.core.service.EnvironmentService;
import com.sas.core.util.ThumbnailUtil.ImageSizeType;
import com.sas.core.util.meta.SasUtil;
import com.sas.core.util.meta.VideoUtil;
import com.sas.core.util.qiniu.Auth;
import com.sas.core.util.qiniu.UrlSafeBase64;

public class QiNiuUtil {

	private static final Logger logger = Logger.getLogger(QiNiuUtil.class);
	
//	public static final void initQiniu(boolean isJapanServer){
//		if(isJapanServer){
//			Config.UP_HOST="http://" + ThirdPardDomain.QiniuUploadForeignDomain.domain;
//		}
//	}
	
	/*********************
	 * 生成上传用的token
	 * @param space
	 * @param fileKey
	 * @param returnUrl
	 * @param returnBody
	 * @return
	 */
	public static final String generateUploadToken(final QiNiuSpace space, final String fileKey,
		final String returnUrl, Map<String, String> returnBody)
	{
		final PutPolicy putPolicy = new PutPolicy(space.bucketName);	
		putPolicy.scope = space.bucketName + (StringUtils.isBlank(fileKey) ? "" : (":" + fileKey));
		putPolicy.expires = space.deadLineSeconds;		
		if(StringUtils.isNotBlank(returnUrl)){
			putPolicy.returnUrl = returnUrl;
		}
		if(returnBody != null){
			putPolicy.returnBody = JsonUtil.getJsonString(returnBody);	
		}
		if(StringUtils.isNotBlank(fileKey)){
			putPolicy.saveKey = fileKey;
		}
		putPolicy.fsizeLimit = space.maxBytes;
		putPolicy.insertOnly = 0; //覆盖式上传
		if(StringUtils.isNotBlank(space.mimeLimit)){
			putPolicy.mimeLimit = space.mimeLimit;
		}
		try {
			return putPolicy.token(ThirdPartConstant.getQiNiuMac());
		} catch (Exception e) {
			logger.error("Fail to generate qiniu token, err="+e.getMessage(), e);
			throw new ImageUploadException("Fail to generate qiniu token, err="+e.getMessage(), e);
		}
	}	

	/****************
	 * 生成视频的key
	 * @param fileKey
	 * @param returnUrl
	 * @param returnBody
	 * @return
	 */
	public static final String generateVideoUploadToken(final QiNiuSpace space, final String fileKey, final String returnUrl)
	{
		final PutPolicy putPolicy = new PutPolicy(space.bucketName);
		final String url = space.bucketName + ":" + fileKey;
		putPolicy.expires = space.deadLineSeconds;		
		if(StringUtils.isNotBlank(returnUrl)){
			putPolicy.returnUrl = returnUrl;
		}
		putPolicy.scope = url;
		if(!VideoUtil.isMP4(fileKey)){
			putPolicy.persistentOps = "avthumb/mp4|saveas/" + Base64.encode(url + ".mp4");
			putPolicy.persistentPipeline = (System.currentTimeMillis()%2 == 0) ? "saihuitongVideoProcess" : "saihuitongVideoProcess2";
		}
		//final Map<String, String> returnBody = new HashMap<String, String>();
		//returnBody.put("fileKey", fileKey);		
		//putPolicy.returnBody = JsonUtil.getJsonString(returnBody);
		
		putPolicy.saveKey = fileKey;
		putPolicy.fsizeLimit = space.maxBytes;
		putPolicy.insertOnly = 0;
		if(StringUtils.isNotBlank(space.mimeLimit)){
			putPolicy.mimeLimit = space.mimeLimit;
		}
		try {
			return putPolicy.token(ThirdPartConstant.getQiNiuMac());
		} catch (Exception e) {
			logger.error("Fail to generate qiniu token, err="+e.getMessage(), e);
			throw new ImageUploadException("Fail to generate qiniu token, err="+e.getMessage(), e);
		}
	}		

	/****************
	 * 生成音频的key
	 * @param fileKey
	 * @param returnUrl
	 * @param returnBody
	 * @return
	 */
	public static final String generateAudioUploadToken(final QiNiuSpace space, final String fileKey, final String returnUrl)
	{
		final PutPolicy putPolicy = new PutPolicy(space.bucketName);
		final String url = space.bucketName + ":" + fileKey;
		putPolicy.expires = space.deadLineSeconds;		
		if(StringUtils.isNotBlank(returnUrl)){
			putPolicy.returnUrl = returnUrl;
		}
		putPolicy.scope = url;
		if(!VideoUtil.isMP3(fileKey)){
			putPolicy.persistentOps = "avthumb/mp3|saveas/" + Base64.encode(url + ".mp3");
			putPolicy.persistentPipeline = (System.currentTimeMillis()%2 == 0) ? "saihuitongAudioProcess" : "saihuitongAudioProcess2";
		}
		putPolicy.saveKey = fileKey;
		putPolicy.fsizeLimit = space.maxBytes;
		putPolicy.insertOnly = 0;
		if(StringUtils.isNotBlank(space.mimeLimit)){
			putPolicy.mimeLimit = space.mimeLimit;
		}
		try {
			return putPolicy.token(ThirdPartConstant.getQiNiuMac());
		} catch (Exception e) {
			logger.error("Fail to generate qiniu token, err="+e.getMessage(), e);
			throw new ImageUploadException("Fail to generate qiniu token, err="+e.getMessage(), e);
		}
	}	
	
	/****************
	 * 生成qrCode图片的文件key
	 * @param sasId
	 * @return
	 */
	public static final String generateQRCodeKey(final long sasId, final QRType type)
	{
		return sasId + "/qrcode/" + type.urlPart + "/" + Long.toHexString(System.currentTimeMillis()) + ".jpg";
	}
	
	/***************
	 * 生成上传文件的key
	 * @param sasId
	 * @param userId
	 * @param type
	 * @return
	 */
	public static final String generateUploadFileKey(final long sasId, final long userId,  final FileType type, 
			final String fileNameExtension)
	{
		return QiNiuUtil.generateUploadFileKey(sasId, userId,  type,  fileNameExtension,  null);
	}
	
	public static final String generateUploadFileKey(final long sasId, final long userId,  final FileType type, 
			final String fileNameExtension, final ImageSizeType imageSizeType)
	{
		final StringBuilder url = new StringBuilder(String.valueOf(sasId)).append("/").append(type.type.toLowerCase()).append("/" + userId);
		if(imageSizeType != null){
			url.append("/").append(imageSizeType.urlPart);
		}		
		url.append("/").append(Long.toHexString(System.currentTimeMillis()));
		if(FileType.Ico == type){
			url.append(".ico");
		}else if(FileType.Logo == type){
			url.append(".jpg");
		}else if(StringUtils.isNotBlank(fileNameExtension)){
			url.append(".").append(processExtension(fileNameExtension.toLowerCase(), "f"));
		} else {
			url.append(".jpg");			
		}
		return url.toString();
	}
	
	public static final String generateCertificationUploadFileKey(final long sasId, final FileType type, 
			final String certificatonNum, final String fileNameExtension)
	{
		final StringBuilder url = new StringBuilder(String.valueOf(sasId))
				.append("/" + Long.toHexString(System.currentTimeMillis()))
				.append("/" + type.type.toLowerCase())
				.append("/" + certificatonNum);
		if(FileType.Ico == type){
			url.append(".ico");
		}else if(FileType.Logo == type){
			url.append(".jpg");
		}else if(StringUtils.isNotBlank(fileNameExtension)){
			url.append(".").append(processExtension(fileNameExtension.toLowerCase(), "f"));
		} else {
			url.append(".jpg");			
		}
		return url.toString();
	}
	
	/************
	 * 处理扩展名
	 * @param fileNameExtension
	 * @return
	 */
	public static final String processExtension(final String fileNameExtension, final String defaultExtension)
	{
		if(fileNameExtension == null){
			return defaultExtension;
		}
		final StringBuilder sb = new StringBuilder("");
		for(int i=0; i<fileNameExtension.length(); i++){
			final char ch = fileNameExtension.charAt(i);
			if((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z')){
				sb.append(ch);
			}else if((ch >= 'A' && ch <= 'Z')){
				sb.append((char)(ch - 'A' + 'a'));
			}
		}
		return sb.length() < 1 ? defaultExtension : sb.toString();
	}	
	
	public static final String generatePortalUploadFileKey(final long siteId, final long userId,  final FileType type, 
			final String fileNameExtension)
	{
		if(FileType.Attachment == type || FileType.Video == type){
			final String url = "biezhaila/" + siteId + "/" + type.type + "/" + userId + "/"
					+ Long.toHexString(System.currentTimeMillis());
			if(StringUtils.isNotBlank(fileNameExtension)){
				return url + "." + processExtension(fileNameExtension, "f");
			}else{
				return url;
			}
		}else if(FileType.Ico == type){
			return "biezhaila/" + siteId + "/" + type.type + "/" + userId + "/" + Long.toHexString(System.currentTimeMillis()) + ".ico";
		}else if(FileType.Logo == type){
			return type.type + "/biezhaila/" + siteId + "/" + userId + "/" + Long.toHexString(System.currentTimeMillis()) + ".jpg";
		}else {
			return "biezhaila/" + siteId + "/" + type.type + "/" + userId + "/" + Long.toHexString(System.currentTimeMillis()) + ".jpg";			
		}
	}
	
	public static final String generateBoxHikingUploadFileKey(final long siteId, final long userId,  final FileType type,
			final String fileNameExtension)
	{
		if(FileType.Attachment == type || FileType.Video == type){
			final String url = "boxhiking/" + siteId + "/" + type.type + "/" + userId + "/"
					+ Long.toHexString(System.currentTimeMillis());
			if(StringUtils.isNotBlank(fileNameExtension)){
				return url + "." + processExtension(fileNameExtension, "f");
			}else{
				return url;
			}
		}else if(FileType.Ico == type){
			return "boxhiking/" + siteId + "/" + type.type + "/" + userId + "/" + Long.toHexString(System.currentTimeMillis()) + ".ico";
		}else if(FileType.Logo == type){
			return type.type + "/boxhiking/" + siteId + "/" + userId + "/" + Long.toHexString(System.currentTimeMillis()) + ".jpg";
		}else {
			return "boxhiking/" + siteId + "/" + type.type + "/" + userId + "/" + Long.toHexString(System.currentTimeMillis()) + ".jpg";
		}
	}
	
	/****************
	 * 上传图片优化
	 * @param space
	 * @param key
	 * @param data
	 * @param maxTryTimes
	 * @return
	 */
	public static final String uploadFile2QiNiu(QiNiuSpace space, final String key, final byte[] data, final int maxTryTimes)
	{
		if(space == null || StringUtils.isBlank(key) || ArrayUtils.isEmpty(data)){
			return null;
		}
		int tryTimes = 0;		
		while((++tryTimes) <= maxTryTimes){
			final InputStream in = new ByteArrayInputStream(data);
			final String url = QiNiuUtil.uploadFile2QiNiu(space, key, in, data.length);
			IOUtil.closeStreamWithoutException(in, null);
			if(url != null && url.length() > 0){
				return url;
			}
			ThreadUtil.sleepNoException(20 * 1000L * tryTimes);
			logger.error("uploadFile2QiNiu Failed, try again, try-times=" + tryTimes);
		}
		logger.error("uploadFile2QiNiu Failed when try times " + maxTryTimes);
		return null;
	}
	
	/******************
	 * 上传文件
	 * @param space
	 * @param key
	 * @param in
	 * @return
	 */
	private static final String uploadFile2QiNiu(QiNiuSpace space, final String key, final InputStream in, final int fileSize)
	{
		if(space == null || StringUtils.isBlank(key) || in == null){
			return null;
		}
		try {
			final String uptoken = QiNiuUtil.generateUploadToken(space, key, null, null);
			final PutExtra extra = new PutExtra();
			final PutRet ret = IoApi.Put(uptoken, key, in, extra);
			if(ret != null && ret.ok()){
				logger.error("Succ upload: " + key + ", fileSize=" + fileSize);
				if(key.startsWith("/")){
					return "http://" + space.domain + key;	
				}else{
					return "http://" + space.domain + "/" + key;					
				}
			}else{
				if(ret == null){
					logger.error("Fail upload: " + key + ", no empty response, fileSize=" + fileSize);
				}else{
					logger.error("Fail upload: " + key + ", response=" + ret.getResponse()
							+", statusCode=" + ret.getStatusCode() + ", fileSize=" + fileSize);
				}	
				return null;
			}	
		}catch (Exception e) {
			logger.error("Fail upload: " + key + ", fileSize=" + fileSize + ", "+ Config.UP_HOST + ", error=" + e.getMessage(), e);
			return null;
		}		
	}
	
	/**********************
	 * 抓取网络图片并上传
	 * @param sasId
	 * @param userId
	 * @param url
	 * @return
	 */
	public static final String migratePictureAndUpload2Qiniu(final long sasId, final long userId, final String url)
	{
		if(StringUtils.isBlank(url)){
			return null;
		}
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			IOUtil.outputDataByDownloadDataFromURL(url, out);
		}catch(Exception ex){
			logger.error("Fail to migratePictureAndUpload2Qiniu: ex=" + ex.getMessage(), ex);
			return null;
		}
		final byte[] contents = out.toByteArray();
		if(ArrayUtils.isEmpty(contents)){
			return null;
		}
		final String key = QiNiuUtil.generateUploadFileKey(sasId, userId, FileType.Image, null);
		return QiNiuUtil.uploadFile2QiNiu(QiNiuSpace.SaihuitongImage, key, contents, 2);
	}
	
	/***************
	 * 获取扩展名
	 * @param fileName
	 * @return
	 */
	public static final String getFileExtension(final String fileName)
	{
		final int index = StringUtils.isBlank(fileName) ? -1 : fileName.lastIndexOf(".");
		return index >= 0 ? processExtension(fileName.substring(index), "") :  "";
	}
	
	/**************
	 * 解析出扩展名和主文件名， 其中key为主文件名， value为扩展名
	 * @param fileName
	 * @return
	 */
	public static final BinaryEntry<String, String> getFileNameDetail(final String fileName)
	{
		if(fileName == null){
			return new BinaryEntry<String, String>("", "");
		}
		final int index = StringUtils.isBlank(fileName) ? -1 : fileName.lastIndexOf(".");
		if(index < 0){
			return new BinaryEntry<String, String>(fileName, fileName);
		}
		return new BinaryEntry<String, String>(fileName.substring(0, index), processExtension(fileName.substring(index), ""));
	}
	
	/*******************
	 * 考虑到客户端传来的文件扩展名可能错的， 这里进行容错
	 * @param fileName
	 * @param extension
	 * @return
	 */
	public static final String generateFileNameExtension(final String fileName, final String extension){
		if(StringUtils.isNotBlank(extension)){
			final int index = extension.lastIndexOf(".");
			return index >= 0 ? processExtension(extension.substring(index), "") :  processExtension(extension, "");
		}else{
			return getFileExtension(fileName);
		}
	}
	
	public static final String removeFileNameExtension(final String fileName, final String defaultFileName){
		if(StringUtils.isNotBlank(fileName)){
			final int index = fileName.lastIndexOf(".");
			return index > 0 ? fileName.substring(0, index):  defaultFileName;
		}else{
			return defaultFileName;
		}
	}
	
	/**************
	 * 生成七牛自定义压缩尺寸图片
	 * @param imgUrl
	 * @param imgSizePostFix
	 * @return
	 */
	public static final String generateQiNiuImgURL(final String imgUrl, final String imgSizePostFix){
		if(QiNiuSpace.isQiNiuImageURL(imgUrl)){
			return imgUrl.replaceAll("(\\-h[0-9]+)|(\\-c*w[0-9]+(h[0-9]+)*)|(\\?imageView.+)", "") + "-" + imgSizePostFix;
		}
		return imgUrl;
	}
	
	public static final List<String> generateQiNiuImgURLs(final List<String> imgUrls, final String imgSizePostFix){
		if(CollectionUtils.isEmpty(imgUrls)){
			return new ArrayList<String>(0);
		}
		final List<String> result = new ArrayList<String>(imgUrls.size());
		for(final String img : imgUrls) {
			result.add( generateQiNiuImgURL(img, imgSizePostFix) );		
		}
		return result;
	}
	
	/*************
	 * 删除压缩样式
	 * @param src
	 * @return
	 */
	public static final String removeCompressStyle(final String src){
		if(QiNiuSpace.isQiNiuImageURL(src)){			
			return src.replaceAll("(\\-h[0-9]+)|(\\-c*w[0-9]+(h[0-9]+)*)|(\\?imageView.+)|(\\?imageMogr2.+)", "");
		}
		return src;
	}
	
	public static final void main(String[] agrs){
		//QiNiuUtil.deleteQiniuResource(QiNiuSpace.SaihuitongImage, "http://img.saihuitong.com/2789/certification/101373/1559573cf4b.jpg-w1024h1024", 2);
		//QiNiuUtil.deleteQiniuResource(QiNiuSpace.SaihuitongImage, "img.saihuitong.com/2789/certification/101373/1559573cf4b.jpg-w1024h1024", 2);
		//QiNiuUtil.deleteQiniuResource(QiNiuSpace.SaihuitongImage, "/2789/certification/101373/1559573cf4b.jpg-w1024h1024",2);
		//QiNiuUtil.deleteQiniuResource(QiNiuSpace.SaihuitongImage, "2789/certification/101373/1559573cf4b.jpg-w1024h1024", 2);
//		final String u = QiNiuUtil.removeCompressStyle("http://img.saihuitong.com/2789/certification/101373/1559573cf4b.jpg-w1024h1024");
//		System.out.println(QiNiuUtil.removeCompressStyle("http://img.saihuitong.com/2789/certification/101373/1559573cf4b.jpg-w1024h1024"));
//		System.out.println(QiNiuUtil.removeCompressStyle("http://img.saihuitong.com/2789/certification/101373/1559573cf4b.jpg-cw1024h1024"));
//		System.out.println(QiNiuUtil.removeCompressStyle("http://img.saihuitong.com/2789/certification/101373/1559573cf4b.jpg-w1024"));
//		System.out.println(QiNiuUtil.removeCompressStyle("http://img.saihuitong.com/2789/certification/101373/1559573cf4b.jpg-h1024"));
		//ThirdPartConstant.setQiNiuAppID("gl9FJf2Wd5vPAkNgWLeFI7E-kWbG5aoxCmFijlHF");
		//ThirdPartConstant.setQiNiuAppSecret("ZHn0aNqeHDgeOO2j18VCmjqgS2vzRan1TqJJBwb1");
		//System.out.println(QiNiuUtil.generateUploadToken(QiNiuSpace.SaihuitongImage, "138/albumimg/292541/15b55c1baeb.jpg", null, null));
		System.out.println(QiNiuUtil.removeCompressStyle("http://img.saihuitong.com/2197/richtext/22/15d62b64c2e.jpg?imageMogr2/thumbnail/400x400!?imageMogr2/thumbnail/400x400!"));
		System.out.println(QiNiuUtil.removeCompressStyle("http://img.saihuitong.com/2197/richtext/22/15d62b64c2e.jpg?imageMogr2/thumbnail/400x400!"));
	}
	
	public static final List<String> removeCompressStyles(final List<String> srcs){
		if(srcs == null){
			return null;
		}
		final List<String> result = new ArrayList<String>(srcs.size());
		for(final String src : srcs){
			result.add(QiNiuUtil.removeCompressStyle(src));
		}
		return result;
	}
	
	/**************
	 * 删除url参数
	 * @param src
	 * @return
	 */
	public static final String removeParams(final String src){
		final int lastIndex = src.indexOf('?');
		if(lastIndex > 0){
			return src.substring(0, lastIndex);
		}			
		return src;
	}
	
	/***********
	 * 生成微信logourl
	 * @param url
	 * @return
	 */
	public static final String addCompressStyleW400H400(final String url)
	{
		if(StringUtils.isBlank(url)){
			return "";
		}
		return url.contains(ImageSizeType.Large.urlPart) ? QiNiuUtil.generateQiNiuImgURL(url, QiNiuImageCompress.cw400h400.style)
				:QiNiuUtil.removeCompressStyle(url) + "?imageMogr2/thumbnail/400x400!";
					
	}
	
	public static final String addCompressStyleW400H400WithNull(final String url)
	{
		if(StringUtils.isBlank(url)){
			return null;
		}
		return url.contains(ImageSizeType.Large.urlPart) ? QiNiuUtil.generateQiNiuImgURL(url, QiNiuImageCompress.cw400h400.style)
				:QiNiuUtil.removeCompressStyle(url) + "?imageMogr2/thumbnail/400x400!";
					
	}
	
	/**************
	 * 过滤出正确的url
	 * @param urls
	 * @return
	 */
	public static final String[] filterValidQiNiuImageUrls(String[] urls) 
	{
		if(ArrayUtils.isEmpty(urls)){
			return new String[0];
		}
		final List<String> result = new LinkedList<String>();
		for(final String url : urls){
			if(ValidatorUtil.URLValidate(url) && QiNiuSpace.isQiNiuImageURL(url)){
				result.add(url.trim());
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	/***************
	 * 删除七牛的资源文件
	 * @return
	 */
	public static final void deleteImageQiniuResourceByAsynchronize(final EnvironmentService environmentService, final QiNiuSpace space, final String url, final int tryTimes)
	{
		if(environmentService.getEnvironmentType() != EnvironmentType.Online){
			return;
		}
		ThreadUtil.execute(new Runnable(){
			public void run()
			{
				QiNiuUtil.deleteImageQiniuResource(environmentService, space, url, tryTimes);
			}
		});
	}
	
	public static final void deleteVideoQiniuResourceByAsynchronize(final EnvironmentService environmentService, final QiNiuSpace space, final String url, 
			final boolean onlyNonMP4File, final int tryTimes)
	{
		if(environmentService.getEnvironmentType() != EnvironmentType.Online){
			return;
		}
		ThreadUtil.execute(new Runnable(){
			public void run()
			{
				QiNiuUtil.deleteVideoQiniuResource(environmentService, space, url, onlyNonMP4File, tryTimes);
			}
		});
	}
	
	public static final void deleteMusicQiniuResourceByAsynchronize(final EnvironmentService environmentService, final QiNiuSpace space, final String url, final int tryTimes)
	{
		if(url == null || url.length() < 1 || SasUtil.isSystemImage(url) || environmentService.getEnvironmentType() != EnvironmentType.Online){
			return;
		}
		ThreadUtil.execute(new Runnable(){
			public void run()
			{
				QiNiuUtil.deleteQiniuResource(space, RequestUtil.removeHttpOrHttps(url), tryTimes);
			}
		});
	}
	
	public static final void deleteAttachmentQiniuResourceByAsynchronize(final EnvironmentService environmentService, final QiNiuSpace space, final String url, final int tryTimes)
	{
		if(url == null || url.length() < 1 || SasUtil.isSystemImage(url) || environmentService.getEnvironmentType() != EnvironmentType.Online){
			return;
		}
		ThreadUtil.execute(new Runnable(){
			public void run()
			{
				QiNiuUtil.deleteQiniuResource(space, RequestUtil.removeHttpOrHttps(url), tryTimes);
			}
		});
	}
	
	public static final void deleteImageQiniuResourcesByAsynchronize(final EnvironmentService environmentService, final QiNiuSpace space, final List<String> urls, 
			final int tryTimes)
	{
		if(CollectionUtils.isEmpty(urls) || environmentService.getEnvironmentType() != EnvironmentType.Online){
			return;
		}		
		ThreadUtil.execute(new Runnable(){
			public void run()
			{
				for(final String url : urls)
				{
					QiNiuUtil.deleteImageQiniuResource(environmentService, space, url, tryTimes);
				}
			}
		});
	}	
	
	public static final void deleteVideoQiniuResourcesByAsynchronize(final EnvironmentService environmentService, final QiNiuSpace space, final List<String> urls, 
			final boolean onlyNonMP4File, final int tryTimes)
	{
		if(CollectionUtils.isEmpty(urls) || environmentService.getEnvironmentType() != EnvironmentType.Online){
			return;
		}		
		ThreadUtil.execute(new Runnable(){
			public void run()
			{
				for(final String url : urls)
				{
					QiNiuUtil.deleteVideoQiniuResource(environmentService, space, url, onlyNonMP4File, tryTimes);
				}
			}
		});
	}	
	
	public static final boolean deleteImageQiniuResource(final EnvironmentService environmentService, final QiNiuSpace space, String url, int tryTimes)
	{
		if(url == null || url.length() < 1 || !QiNiuSpace.isQiNiuImageURL(url) || SasUtil.isSystemImage(url) || environmentService.getEnvironmentType() != EnvironmentType.Online){
			return false;
		}
		return QiNiuUtil.deleteQiniuResource(space, 
				RequestUtil.removeHttpOrHttps(QiNiuUtil.removeCompressStyle(url)), tryTimes);
	}
	
	/***************
	 * 删除七牛的资源文件
	 * @return
	 */
	public static final boolean deleteVideoQiniuResource(final EnvironmentService environmentService, final QiNiuSpace space, String url, final boolean onlyNonMP4File,
			int tryTimes)
	{
		if(url == null || url.length() < 1 || SasUtil.isSystemImage(url) || environmentService.getEnvironmentType() != EnvironmentType.Online){
			return false;
		}
		if(onlyNonMP4File && VideoUtil.isMP4(url)){
			return false;
		}
		return QiNiuUtil.deleteQiniuResource(space, RequestUtil.removeHttpOrHttps(url), tryTimes);
	}
	/***************
	 * 删除七牛的资源文件
	 * @return
	 */
	private static final boolean deleteQiniuResource(final QiNiuSpace space, String url, int tryTimes)
	{
		url = url.replace(space.domain, "");
		url = url.startsWith("/") ? url.substring(1) : url;
		final Mac mac = ThirdPartConstant.getQiNiuMac();
		final Auth auth = Auth.create(mac.accessKey, mac.secretKey);	
		//指定需要删除的空间和文件，格式为： <bucket>:<key>, 通过安全base64编码方式进行编码处理
        final String encodedEntryURI = UrlSafeBase64.encodeToString(space.bucketName + ":" + url);
        //指定接口
        final String target = "/delete/" + encodedEntryURI + "\n";
        //获取token，即操作凭证
        final String access_token = auth.sign(target);
		//指定好请求的delete接口地址
		do{
			PostMethod postMethod = null;
			try{
		        //指定好请求的delete接口地址
				final HttpClient httpClient = new HttpClient();
				postMethod = new PostMethod("http://rs.qiniu.com/delete/" + encodedEntryURI);
				postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");    
				postMethod.addRequestHeader("Authorization", "QBox " + access_token);
				if (httpClient.executeMethod(postMethod) != 200) {
					logger.error("Qiniu delete failed : "+url+", status=" + postMethod.getStatusLine());
					return false;
				}else{
					logger.error("Qiniu delete success : " + url);
				}
				return true;
			}catch(Exception ex){
				logger.error("Fail to delete resource,  ex=" + ex.getMessage(), ex);
				ThreadUtil.sleepNoException(Miliseconds.TenSeconds.miliseconds);
			}finally{
				if(postMethod != null){
					postMethod.abort();
				}
			}
		}while(--tryTimes > 0);
		return false;
	}

	/************
	 * 生成图片的hashcode， 唯一性
	 * @param url
	 * @return
	 */
	public static final String generateImageURLHashcode(final String url){
		return QiNiuUtil.generateURLHashcode(QiNiuUtil.removeCompressStyle(url));
	}
	
	public static final String generateVideoURLHashcode(final String url){
		return QiNiuUtil.generateURLHashcode(VideoUtil.checkMP4Url(url));
	}
	
	private static final String generateURLHashcode(final String url)
	{
		final StringBuilder oddChars = new StringBuilder("");
		final StringBuilder evenChars = new StringBuilder("");
		final char[] array = url.toCharArray();
		for(int i = 0; i<array.length; i++)
		{
			if(i % 2 == 0){
				evenChars.append(array[i]);	
			}else{
				oddChars.append(array[i]);
			}
		}
		final String result = MD5Util.getInstance().get32MD5ofStr(oddChars.toString())
				+ MD5Util.getInstance().get32MD5ofStr(evenChars.toString())
				+ MD5Util.getInstance().get32MD5ofStr(url);
		return result.replaceAll("[0]+", "0");
	}
	
}