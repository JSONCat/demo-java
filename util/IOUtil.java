/**
 * 
 */
package com.sas.core.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.sas.core.dto.BinaryEntry;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.ThirdPartConstant.FileType;
import com.sas.core.constant.ThirdPartConstant.QiNiuSpace;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.meta.QRCode;
import com.sas.core.service.EnvironmentService;
import com.sas.core.util.ThumbnailUtil.ImageSizeType;

/**
 * 网络以及file 以及流相关的都放在这里
 * @author zhuliming
 *
 */
public class IOUtil {

	private static final Logger logger = Logger.getLogger(IOUtil.class);
	
	private static final int BUFFER_SIZE = 8192;  
	
	private static String localIP = null; //本机ip

	//每个IP的图片下载次数监控
	private static final LRUMap imgDownloadTimeCachePerHour = new LRUMap(10000); //每小时的下载次数的控制;

	
	/***************
	 * 判断每个IP是否下载太多了次数
	 * @param request
	 * @return
	 */
	public static final synchronized boolean isImgDownloadAccessMaxLimitPerIP(final HttpServletRequest request)
	{
		final String ip = RequestUtil.getUserIPFromRequest(request);
		final String key = ip + "_" + String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
		final Long times = (Long)imgDownloadTimeCachePerHour.get(key);
		if(times != null){
			imgDownloadTimeCachePerHour.put(key, times + 1L);
			if(times >= 500){
				return true;
			}
		}else{
			imgDownloadTimeCachePerHour.put(key, 1L);
		}
		return false;
	}
	
	/**************
	 * 关闭文件对象
	 * @param reader
	 */
	public static final void closeReaderWithoutException(final BufferedReader reader){
		if(reader != null){
			try {
				reader.close();
			} catch (IOException ex) {
				logger.error("Fail to close reader after reading file: ex="+ex.getMessage(), ex);	
			}
		}
	}
	
	public static final void closeStreamWithoutException(final InputStream in, final OutputStream out){
		if(in != null){
			try {
				in.close();
			} catch (IOException ex) {
				logger.error("Fail to close InputStream: ex="+ex.getMessage(), ex);	
			}
		}
		if(out != null){
			try {
				out.close();
			} catch (IOException ex) {
				logger.error("Fail to close OutputStream: ex="+ex.getMessage(), ex);	
			}
		}
	}	
	
	public static final void closeStreamWithoutException(final HttpURLConnection httpUrl){
		if(httpUrl != null){
			try {
				httpUrl.disconnect();
			} catch (Exception ex) {
				logger.error("Fail to close HttpURLConnection: ex="+ex.getMessage(), ex);	
			}
		}
	}
	
	/************
	 * 获取换行符
	 * @return
	 */
	public static final String lineSeparator(){
		return System.getProperty("line.separator", "\n"); 
	}
	
	/**
	 * 单线程，抓取非七牛的图片, 参数tryTimesOfEachURL为抓取每个图片时， 每个图片的尝试次数.
	 * 返回的map中key为老的url， value为新的七牛url
	 * @param imageSrcList
	 * @return
	 */
	public static final Map<String,String> migratePicturesToQiniu(final long sasId, final long userId, 
			List<String> imageSrcList, final int tryTimesOfEachURL)
	{
		//设定返回Map
		final Map<String, String> replaceMap = new HashMap<String, String>(imageSrcList.size());
		for(final String url :  imageSrcList)
		{
			try{
				if(StringUtils.isNotBlank(url)){
					String newURL = IOUtil.migrateOnePictureToQiniu(sasId, userId, url, tryTimesOfEachURL);
					if(StringUtils.isBlank(newURL)){//试试微信
						final String url2 = IOUtil.processWeixinImageURL(url);
						if(!url.equalsIgnoreCase(url2)){
							newURL = IOUtil.migrateOnePictureToQiniu(sasId, userId, url2, tryTimesOfEachURL);
						}
					}
					if(StringUtils.isNotBlank(newURL)){
						replaceMap.put(url, newURL);
					}
				}
			}catch(Exception ex){
				logger.error("Fail to grap picture:" + url + ", try next one!", ex);
			}
		}
		return replaceMap;
	}
	
	/*****************
	 * 抓取一个图片， 并上传到七牛， 参数tryTimes为失败时的重试次数， 必须为大于0的整数
	 * 失败返回null
	 * @param imageURL
	 * @param tryTimes
	 * @return
	 */
	public static final String migrateOnePictureToQiniu(final long sasId, final long userId, String imageURL, 
			final int totalTryTimes)
	{
		//开始抓图
		int times = 0;			
		while(times <= totalTryTimes){
			ByteArrayOutputStream baos = null;
			try{
				logger.error("migrateOnePictureToQiniu: " + imageURL + ", times:" + times);
				baos = new ByteArrayOutputStream();
				final ImageSizeType imageSizeType = IOUtil.outputImageByDownloadDataFromURL(RequestUtil.removeInValidURLChars(imageURL), baos);
				if(imageSizeType != null)
				{
					// 生成图片KEY
					final String fileKey = QiNiuUtil.generateUploadFileKey(sasId, userId,  FileType.Image,  null, imageSizeType);
					final String newUrl = QiNiuUtil.uploadFile2QiNiu(QiNiuSpace.SaihuitongImage, fileKey,  baos.toByteArray(), 3);				
					//如果无法上传完成则不替换, 上传失败则重试， 概率很低
					if(StringUtils.isNotBlank(newUrl)){	
						return newUrl;
					}		
				}
			}catch(Exception ex){
				logger.error("Failed to migrate picture, url="+imageURL+", ex=" + ex.getMessage() , ex);
			}finally{
				IOUtil.closeStreamWithoutException(null, baos);
			}
			times ++;
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds * times);
		}
		return null;
	}
	
	/*************
	 * 抓图并考虑微信情况
	 * @param sasId
	 * @param userId
	 * @param imageURL
	 * @param totalTryTimes
	 * @return
	 */
	public static final String migrateOnePictureToQiniuByCaringWeixin(final long sasId, final long userId, 
			final String imageURL, final int totalTryTimes)
	{
		String newURL = IOUtil.migrateOnePictureToQiniu(sasId, userId, imageURL, totalTryTimes);
		if(StringUtils.isNotBlank(newURL)){
			logger.error("grap picture succ1 >> " + imageURL);
			return newURL;
		}
		//抓取失败
		logger.error("grap picture fail1 >> " + imageURL);	
		final String url2 = IOUtil.processWeixinImageURL(imageURL);	
		if(!imageURL.equalsIgnoreCase(url2)){
			newURL = IOUtil.migrateOnePictureToQiniu(sasId, userId, url2, 2);
			if(StringUtils.isNotBlank(newURL)){
				logger.error("grap picture succ2 >> " + url2);
				return newURL;
			}	
			logger.error("grap picture fail2 >> " + imageURL);	
		}
		return null;
	}
	
	/**************
	 * 处理微信图片url
	 * @param imageURL
	 * @return
	 */
	public static final String processWeixinImageURL(final String imageURL)
	{
		if(StringUtils.isBlank(imageURL)){
			return null;
		}
		//http://mmbiz.qpic.cn/mmbiz/9rlObxiaAApgCt6fuicxN9WAr2Sib58keyrATibTErN6gj4CYVyvGVicZyGvuCE5RRYDyo4GPSGX02hBAuwKrPwHC0Q/640?wx_fmt=jpeg&tp=webp&wxfrom=5
		int index = imageURL.indexOf("mmbiz.q"); //mmbiz.qpic.cn
		if(index >= 0){
			final StringBuilder sb = new StringBuilder(imageURL.substring(0, index));
			for(; index<imageURL.length(); index++)
			{
				final char ch = imageURL.charAt(index);
				if(ch == '-' || ch == '?' || ch == '"' || ch == '\'' || ch == ' ' || ch == '>' || sb.length() >= 1000){
					break;
				}
				sb.append(ch);
			}			
			final String url = sb.toString();
			if(url.endsWith("/0")){
				return url.substring(0, url.lastIndexOf("/0")) + "/640";
			}else{
				return url;
			}
		}
		return imageURL;
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println(IOUtil.isResourceAvaiable("http://v.saihuitong.com/8/video/183420/15a26a7a827.mp4?vframe/jpg/offset/5/w/200"));	
	}

	/****************
	 * 从in中读取数据， 输入到out中
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	public static final void outputFromInputStream(final InputStream in, final OutputStream out) throws Exception{
		final byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;  
		while ((size = in.read(buf)) > 0) {  
			out.write(buf, 0, size);  
		}
		out.flush();
		IOUtil.closeStreamWithoutException(in, out);
	}

	public static final void outputFromBytes(final byte[] bytes, final OutputStream out) throws Exception{
		if(ArrayUtils.isEmpty(bytes)){
			return;
		}
		out.write(bytes, 0, bytes.length); 
		out.flush();
		IOUtil.closeStreamWithoutException(null, out);
	}
	
	/**************
	 * 读取二进制对象
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static final byte[] outputFromInputStream(final InputStream in, final boolean checkAvailableBytesAndReset) throws Exception{
		if(in == null){
			return null;
		}
		if(checkAvailableBytesAndReset && in.available() < 1){
			in.reset();
			if(in.available() < 1){
				return null;
			}
		}
		return IOUtil.input2byte(in);
	}
	
	/**************
	 * 将流转成字节数
	 * @param inStream
	 * @return
	 * @throws IOException
	 */
	public static final byte[] input2byte(InputStream in)  
            throws IOException {  
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;  
		while ((size = in.read(buf)) != -1) {  
			out.write(buf, 0, size);  
		}
		IOUtil.closeStreamWithoutException(in, null);
		return out.toByteArray();
    } 

	
	/**************
	 * 读取网络流并输出给output
	 * @param url
	 * @param out
	 * @throws Exception
	 */
	public static final byte[] downloadDataFromURL(final String url){
		HttpURLConnection connection  = null;
		try{
			connection  = (HttpURLConnection) new URL(url).openConnection();  
			connection.setDoOutput(false);  
	        connection.setDoInput(true);  
	        connection.setRequestMethod("GET");  
	        connection.setConnectTimeout((int)Miliseconds.TweentySeconds.miliseconds); //20秒连接
	        connection.setReadTimeout((int)Miliseconds.OneMinute.miliseconds); //5分钟
	        connection.setUseCaches(false);  
//	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  
	        connection.setRequestProperty("User-Agent","Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");  
	        connection.setRequestProperty("Accept-Language","zh-CN");  
//	        connection.setRequestProperty("Accept-Encoding","gzip, deflate");
			connection .connect();
			return IOUtil.outputFromInputStream(connection.getInputStream(), false);
		}catch(Exception ex){
			logger.info("Failed read http url, url="+url+" , ex=" + ex.getMessage() , ex);
			return null;
		}finally{
			IOUtil.closeStreamWithoutException(connection);
		}
	}
	
	
	public static final void outputDataByDownloadDataFromURL(final String url, final OutputStream out) throws Exception{
		try{
			final byte[] imageData = IOUtil.downloadDataFromURL(url);
			if(ArrayUtils.isEmpty(imageData)){
				return;
			}
			out.write(imageData);
			out.flush();		
		}catch(Exception ex){
			logger.info("Failed outputDataByDownloadDataFromURL, url="+url+" , ex=" + ex.getMessage() , ex);
		}
	}
	
	public static final ImageSizeType outputImageByDownloadDataFromURL(final String url, final OutputStream out) throws Exception{
		try{
			final byte[] imageData = IOUtil.downloadDataFromURL(url);
			return IOUtil.outputImageData(imageData, out);
		}catch(Exception ex){
			logger.info("Failed outputImageByDownloadDataFromURL, url="+url+" , ex=" + ex.getMessage() , ex);
			return null;
		}
	}
	
	private static final ImageSizeType outputImageData(final byte[] imageData, final OutputStream out) throws Exception{
		try{
			if(ArrayUtils.isEmpty(imageData)){
				return null;
			}
			final BufferedImage image = ThumbnailUtil.readBufferedImage(imageData);	
			if(image == null){
				return null;
			}
			final ImageSizeType resultImageSizeType = ImageSizeType.parse(image.getWidth(), image.getHeight());			
			out.write(imageData);
			out.flush();
			return resultImageSizeType;
		}catch(Exception ex){
			logger.info("Failed outputImageByDownloadDataFromURL, ex=" + ex.getMessage() , ex);
			return null;
		}
	}
	
	/***********
	 * 网络资源十分是可获取的
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static final boolean isResourceAvaiable(final String url) {
		HttpURLConnection connection  = null;
		try{
			connection  = (HttpURLConnection) new URL(url).openConnection(); 
			connection.connect();
			return connection.getResponseCode() == 200;
		}catch(Exception ex){
			logger.error("isResourceAvaiable-" + url, ex);
			return false;
		}finally{
			IOUtil.closeStreamWithoutException(connection );
		}
	}
	
	/************
	 * 是否资源 不可获取或者长度为0
	 * @param url
	 * @param tryTimes
	 * @return
	 */
	public static final boolean isResourceNotAvaiableORZeroSize(final String url, int tryTimes)
	{
		int times = 0;
		do{
			times ++;
			HttpURLConnection httpUrl = null;
			InputStream inputStream = null;
			try{				
				httpUrl = (HttpURLConnection) new URL(url).openConnection();  
				httpUrl.connect();
				if(httpUrl.getResponseCode() == 404){
					return true;
				}
				inputStream = httpUrl.getInputStream();
				if(inputStream != null){
					return inputStream.available() < 1;
				}
				ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds * times);
			}catch(Exception ex){
				logger.error("isResourceNotAvaiableORZeroSize -" + url, ex);
				ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds * times);
			}finally{
				IOUtil.closeStreamWithoutException(inputStream, null);
				IOUtil.closeStreamWithoutException(httpUrl);
			}	
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds * times);
		}while(tryTimes > times);		
		return true;
	}
	
	/**
	 * @Title: isValidFileName
	 * @Description: 判断是否是合法的文件名
	 * @param fileName
	 * @return
	 * @throws
	 */
	public static boolean isValidFileName(String fileName){
		if (fileName == null || fileName.length() > 255 || !fileName.contains(".")){
	    	return false;
	    }else{
	    	return fileName.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
	    }
	}
	
	/**
	 * @Title: isXlsFileName
	 * @Description: 判断是否合法的EXCEL文件
	 * @param fileName
	 * @return
	 * @throws
	 */
	public static boolean isXlsFileName(String fileName){
		return fileName.toLowerCase().matches("^.+\\.(?i)((xls)|(xlsx))$");
	}
	
	public static boolean is2007XlsFileName(String fileName){
		return fileName.toLowerCase().endsWith("xlsx");
	}
	
	public static boolean is2003XlsFileName(String fileName){
		return fileName.toLowerCase().endsWith("xls");
	}
	
	/**
	 * 将InputStream转换成ByteArrayOutputStream
	 * @return
	 */
	public static ByteArrayOutputStream convertInputStreamToByteArrayOutputStream(InputStream inputStream){
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];  
			int len;  
			while ((len = inputStream.read(buffer, 0, BUFFER_SIZE)) > -1 ) {  
				out.write(buffer, 0, len);  
			}
			out.flush();
			return out;
		} catch (IOException e) {
			logger.error("Failed to convert InputStream to ByteArrayOutputStream with IOException : " + e, e);
			return null;
		} finally{
			IOUtil.closeStreamWithoutException(inputStream, null);
		}
	}
	
	/*****************
	 * 简单的文件保存函数， 主要用于测试
	 * @param filePath
	 * @param data
	 * @throws Exception
	 */
	public static final void saveBinaryData2File(final String filePath, final byte[] data) throws Exception{
		final File f = new File(filePath);
		f.createNewFile();
		final FileOutputStream out = new FileOutputStream(f);
		out.write(data);
		out.flush();
		IOUtil.closeStreamWithoutException(null, out);
	}
	
	
	/*******************
	 * 读取resource目录下面的资源文件
	 * 例如参数/core/activity_address_tag.txt
	 * @return
	 */
	public static final String readTextFileUnderClassPath(final String path){
		if(StringUtils.isBlank(path)){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");
		BufferedReader reader = null;
		InputStream inputStream = null;
		try{
			inputStream = IOUtil.class.getResourceAsStream(path);
			reader = new BufferedReader(new InputStreamReader(inputStream, Encoding.UTF8.type));
			String line = null;
			while((line = reader.readLine()) != null){
				sb.append(line + "\n");
			}
		}catch(Exception ex){
			logger.error("Fail to readTextFileUnderClassPath: ex="+ex.getMessage(), ex);	
		}finally{
			IOUtil.closeReaderWithoutException(reader);
			IOUtil.closeStreamWithoutException(inputStream, null);
		}		
		return sb.toString();		
	}
	
	public static final String readTextFile(final File f){
		if(f == null){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");
		InputStream inputStream = null;
		BufferedReader reader = null;
		try{
			inputStream = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(inputStream, Encoding.UTF8.type));
			String line = null;
			while((line = reader.readLine()) != null){
				sb.append(line + "\n");
			}
		}catch(Exception ex){
			logger.error("Fail to readTextFile: ex="+ex.getMessage(), ex);	
		}finally{
			IOUtil.closeReaderWithoutException(reader);
			IOUtil.closeStreamWithoutException(inputStream, null);
		}		
		return sb.toString();		
	}
	
	public static final byte[] readByteFile(final File f){
		if(f == null){
			return null;
		}
		InputStream inputStream = null;
		ByteArrayOutputStream out = null;
		try{
			inputStream = new FileInputStream(f);
			out = new ByteArrayOutputStream();
			final byte[] buf = new byte[BUFFER_SIZE];
			int size = 0;  
			while ((size = inputStream.read(buf)) != -1) {  
				out.write(buf, 0, size);  
			}
			return out.toByteArray();
		}catch(Exception ex){
			logger.error("Fail to readByteFile: ex="+ex.getMessage(), ex);	
			return null;
		}finally{
			IOUtil.closeStreamWithoutException(inputStream, out);
		}
	}
	
	public static final String readTextFileUnderInputStream(final InputStream inputStream, final Encoding encoding, 
			final String lineDividerChar)
	{
		if(inputStream == null){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(inputStream, encoding.type));
			String line = null;
			while((line = reader.readLine()) != null){
				sb.append(line);
				sb.append(lineDividerChar);
			}
		}catch(Exception ex){
			logger.error("Fail to readTextFileUnderClassPath: ex="+ex.getMessage(), ex);	
		}finally{
			IOUtil.closeReaderWithoutException(reader);
			IOUtil.closeStreamWithoutException(inputStream, null);
		}		
		return sb.toString();		
	}
	
	/*****************
	 * 从网站下载HTTP流量
	 * @param url
	 * @param tryTimes
	 * @return
	 * @throws Exception
	 */
	public static final byte[] readFromHttpURL(final EnvironmentService environmentService,
			final boolean usingLocalFileCache, final String url, final int tryTimes) 
	{
		File eventScoreBackgroundImageFile = null;
		//启用本地缓存，则先读取试试
		if(usingLocalFileCache && environmentService != null){
			final File eventScoreBackgroundImageDirectory = new File(environmentService.getEventScoreBackgroundImageDirectory());
			eventScoreBackgroundImageFile = new File(eventScoreBackgroundImageDirectory, QRCode.encryptingTo128Str(url));
			if(eventScoreBackgroundImageFile.exists() && eventScoreBackgroundImageFile.isFile() 
					&& eventScoreBackgroundImageFile.canRead() && eventScoreBackgroundImageFile.length() > 1024){
				return IOUtil.readByteFile(eventScoreBackgroundImageFile);
			}
		}
		int times = 0;
		do{
			times ++;
			HttpURLConnection httpUrl = null;
			InputStream inputStream = null;
			ByteArrayOutputStream out = null;
			try{				
				httpUrl = (HttpURLConnection) new URL(url).openConnection();  
				httpUrl.connect();
				inputStream = httpUrl.getInputStream();
				if(inputStream != null && inputStream.available() > 0){
					out = new ByteArrayOutputStream();
					final byte[] buf = new byte[BUFFER_SIZE];
					int size = 0;  
					while ((size = inputStream.read(buf)) != -1) {  
						out.write(buf, 0, size);  
					}
					//启用本地缓存，则需要缓存到本地
					if(usingLocalFileCache && environmentService != null){
						if(eventScoreBackgroundImageFile.exists()){
							IOUtil.dropFile(eventScoreBackgroundImageFile);
						}
						eventScoreBackgroundImageFile.createNewFile();
						final byte[] data = out.toByteArray();
						IOUtil.outputFromBytes(data, new FileOutputStream(eventScoreBackgroundImageFile));
						return data;
					}else{
						return out.toByteArray();
					}
				}
			}catch(Exception ex){
				logger.error("Failed read http url, url="+url+" , ex=" + ex.getMessage() , ex);
			}finally{
				IOUtil.closeStreamWithoutException(inputStream, out);
				IOUtil.closeStreamWithoutException(httpUrl);
			}	
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds * times);
		}while(tryTimes > times);		
		return null;
	}
	
	/*****************
	 * 从网站下载HTTP流量 保存成key/Map= fileName/byte[]
	 * @param url
	 * @param tryTimes
	 * @return
	 * @throws Exception
	 */
	public static final BinaryEntry<String, byte[]> readBinaryEntryFromHttpURL(final String url, final int tryTimes)
	{
		int times = 0;
		do{
			times ++;
			HttpURLConnection httpUrl = null;
			InputStream inputStream = null;
			ByteArrayOutputStream out = null;
			try{
				httpUrl = (HttpURLConnection) new URL(url).openConnection();
				httpUrl.connect();
				inputStream = httpUrl.getInputStream();
				if(inputStream != null && inputStream.available() > 0){
					out = new ByteArrayOutputStream();
					final byte[] buf = new byte[BUFFER_SIZE];
					int size = 0;
					while ((size = inputStream.read(buf)) != -1) {
						out.write(buf, 0, size);
					}
          final String contentType = httpUrl.getContentType();
					if (StringUtils.isBlank(contentType)){
					  logger.error("Failed to read content type, url = " + url);
					  return null;
          }
          final String fileExt = contentType.substring(contentType.indexOf("/") + 1);
          return  new BinaryEntry<String, byte[]>(parseFileNameFromURL(url) + "." + fileExt, out.toByteArray());
				}
			}catch(Exception ex){
				logger.error("Failed read http url and file name, url="+url+" , ex=" + ex.getMessage() , ex);
			}finally{
				IOUtil.closeStreamWithoutException(inputStream, out);
				IOUtil.closeStreamWithoutException(httpUrl);
			}
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds * times);
		}while(tryTimes > times);
		return null;
	}
	
	/************
	 * 读取流  转成string
	 * @param inputStream
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static final String readTextFromHttpURL(final InputStream inputStream, final Encoding encoding) throws Exception{
		ByteArrayOutputStream baos = null;
		try{
			baos = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			return new String(baos.toByteArray(), encoding.type);
		}finally{
			IOUtil.closeStreamWithoutException(inputStream, baos);
		}
	}
	
	/*****************
	 * 从网站下载HTTP流量
	 * @param url
	 * @param tryTimes
	 * @return
	 * @throws Exception
	 */
	public static final InputStream readInputStreamFromHttpURL(final String url, int tryTimes)  {
		do{
			HttpURLConnection httpUrl = null;
			InputStream inputStream = null;
			try{				
				httpUrl = (HttpURLConnection) new URL(url).openConnection();  
				httpUrl.connect();
				inputStream = httpUrl.getInputStream();
				if(inputStream != null && inputStream.available() > 0){
					return inputStream;
				}
				ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			}catch(Exception ex){
				logger.info("Failed read http InputStreamFromHttpURL, url="+url+" , ex=" + ex.getMessage() , ex);
				ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			}finally{
				IOUtil.closeStreamWithoutException(httpUrl);
			}	
		}while(--tryTimes > 0);		
		return null;
	}
	
	public static final String readTextFromHttpURL(final String url, int tryTimes)  
	{
		do{
			HttpURLConnection httpUrl = null;
			try{				
				httpUrl = (HttpURLConnection) new URL(url).openConnection();  
				httpUrl.connect();
				final InputStream inputStream = httpUrl.getInputStream();
				if(inputStream != null && inputStream.available() > 0){
					return IOUtil.readTextFromHttpURL(inputStream, Encoding.UTF8);
				}
				ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			}catch(Exception ex){
				logger.info("Failed read http InputStreamFromHttpURL, url="+url+" , ex=" + ex.getMessage() , ex);
				ThreadUtil.sleepNoException(Miliseconds.TenSeconds.miliseconds);
			}finally{
				IOUtil.closeStreamWithoutException(httpUrl);
			}
		}while(--tryTimes > 0);		
		return null;
	}
	
	/************
	 * 确保文件夹存在
	 * @param file
	 */
	public static final void makeSureDirectoryExists(File dir){
		if( ! dir.exists()){
			dir.mkdir();
		}else if( ! dir.isDirectory()){
			dir.delete();
			dir.mkdir();
		}
	}
	
	/**************
	 * 获取本机IP
	 * @return
	 */
	public static final String getLocalIP()
	{
		if(localIP != null){
			return localIP;
		}
		try{
			final InetAddress addr = InetAddress.getLocalHost();
			localIP = addr.getHostAddress().toString();
			return localIP;
		}catch(Exception ex){
			logger.error("Fail to getLocalIP, ex=" + ex.getMessage(), ex);
			return "121.42.233.65";
		}		
	}
	
	/**********
	 * 从url中解析文件名字
	 * @param url
	 * @return
	 */
	public static final String parseFileNameFromURL(String url){
		while(url.length() > 0 && url.endsWith("/")){
			url = url.substring(0, url.length()-1);
		}
		final int index = url.lastIndexOf("/");
		return index < 0 ? url : url.substring(index+1);
	}
	
	/**
   * 读取url中的文件中的扩展名 支持jpg、png、pdf、zip
   * @param url
   * @return
   */
	public static final String parseFileExtFromUrl(final String url){
		HttpURLConnection connection  = null;
		try {
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.connect();
      final String contentType = connection.getContentType();
      if (StringUtils.isBlank(contentType)) {
        logger.error("Failed to parse file extension, url= " + url);
        return null;
      }else {
        return contentType.substring(contentType.indexOf("/") + 1);
      }
    }catch (Exception ex){
		  logger.info("Failed to parse content type, url="+url+" , ex=" + ex.getMessage() , ex);
		  return null;
    }finally {
		  IOUtil.closeStreamWithoutException(connection);
    }
	}
	
	/**************
	 * 递归删除整个文件或者目录【包括子目录以及子目录下面的文件】
	 * @param dir
	 * @return
	 */
	public static final int dropFile(final File dir){
		if(!dir.exists()){
			return 0;
		}
		if(dir.isFile()){
			dir.delete();
			return 1;
		}
		int result = 0;
		for(final File f : dir.listFiles()){
			if(f.isFile()){//不走递归
				f.delete();
				result ++;
			}else{
				result += dropFile(f);
			}
		}
		dir.delete();
		return result;
	}
	/*****************
	 * 是否是zip file
	 * @param url
	 * @return
	 */
	public static final boolean isImageFile(String url)
	{
		if(url == null){
			return false;
		}
		url = url.toLowerCase();
		final int oldLength = url.length();
		url = url.replaceAll("(\\.bmp)|(\\.png)|(\\.gif)|(\\.jpg)|(\\.jpeg)", "");
		return url.length() != oldLength;
	}
}