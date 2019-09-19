/**
 * @Title: ThumbnailUtil.java
 * @Package com.sas.core.util
 * @Description: 赛会通的缩略图简易生成器
 * @author yunshang_734@163.com
 * @date 2015-1-7 下午4:11:03
 * @version V1.0
 */
package com.sas.core.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.sas.core.constant.SasConstant;
import com.sas.core.constant.SasConstant.WatermarkPosition;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;

import com.sas.core.constant.AlbumConstant;
import com.sas.core.constant.ActivityScoreConstant.ScoreFieldFont;
import com.sas.core.constant.CommonConstant.BinaryState;
import com.sas.core.constant.CommonConstant.PageLimit;
import com.sas.core.constant.ThirdPartConstant.QiNiuSpace;

/**
 * @ClassName: ThumbnailUtil
 * 缩略图生成器， 对于负载很低， 比如一天在5000张以内的缩略图生成可以
 * 调用此接口
 * @author yunshang_734@163.com
 * @date 2015-1-7 下午4:11:03
 
 * @author zhuliming@corp.netease.com
 *
 */
public final class ThumbnailUtil {
	
	private static final Logger logger = Logger.getLogger(ThumbnailUtil.class);
	
	
	/**************
	 * 图片的大小类型，小图不会出现在列表中
	 * @author Administrator
	 *
	 */
	public static enum ImageSizeType{
		Small(149, 149, "small"),
		Middle(499, 499, "middle"),
		Large(-1, -1, "large");
		private final int maxWidth;
		private final int maxHeight;
		public final String urlPart;
		private ImageSizeType(final int maxWidth, final int maxHeight, final String urlPart){
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
			this.urlPart = urlPart;
		}
		public static final ImageSizeType parse(final int width, final int height)
		{
			if(Small.maxWidth >= width || Small.maxHeight >= height){
				return Small;
			}else if(Middle.maxWidth >= width || Middle.maxHeight >= height){
				return Middle;
			}else{
				return Large;
			}
		}
		public boolean isMySize(final String url){ //是否url包含了自己
			return url != null && url.contains(this.urlPart);
		}
	}
	
	/************************
	 * 支持输出的图片流类型
	 * @author Administrator
	 */
	public static enum ImageType{
		BMP("bmp"), JPG("jpg"), WBMP("wbmp"), PNG("png"), JPEG("jpeg"), GIF("gif");
		public String extention;
		private ImageType(String extention){
			this.extention = extention;
		}
	}
	
	/*****************
	 * 图片裁剪时的图片选取相对位置
	 * @author Administrator
	 *
	 */
	public static enum ImageCutPosition{
		TOP_LEFT(10), TOP_CENTER(13), TOP_RIGHT(15), 
		CENTER_LEFT(20), CENTER(22), CENTER_RIGHT(24), 
		BOTTOM_LEFT(30), BOTTOM_CENTER(33), BOTTOM_RIGHT(35);
		
		public final Positions position;
		private ImageCutPosition(int v){
			Positions pos = null;
			switch(v)
			{
				case 10: pos = Positions.TOP_LEFT; break;
				case 13: pos = Positions.TOP_CENTER; break;
				case 15: pos = Positions.TOP_RIGHT; break;
				
				case 20: pos = Positions.CENTER_LEFT; break;
				case 22: pos = Positions.CENTER; break;
				case 24: pos = Positions.CENTER_RIGHT; break;
				
				case 30: pos = Positions.BOTTOM_LEFT; break;
				case 33: pos = Positions.BOTTOM_CENTER; break;
				default: pos = Positions.BOTTOM_RIGHT;
			}
			
			position = pos;
		}
	}
	
	
	/************
	 * <P>判断缩略图长宽是否正常</P>
	 * @param thumbMaxWidth
	 * @param thumbMaxHeight
	 * @return
	 */
	private static final boolean isThumbSizeValid(final int thumbMaxWidth, final int thumbMaxHeight){
		return thumbMaxWidth >= 2 && thumbMaxHeight >= 2;
	}
	
	/*********************
	 * <P>参数是否存在为null</P>
	 * @param objs
	 * @return
	 */
	private static final boolean isParamObjectsNotNull(final Object... objs)
	{
		for(Object obj: objs)
		{
			if(obj == null){
				return false;
			}
		}
		return true;
	}
	
	/*************************
	 * <P>对参数originFile指定的图片进行等比压缩，压缩后的图片会被保存到destFile, 注意:
	 * 1. destFile如果已经存在， 就会直接覆盖，否则就会新建;
	 * 2. thumbMaxWidth,thumbMaxHeight是缩略图的最大高度和宽度，由于图片是等比压缩，
	 *    生成的缩略图在等比压缩时，会保证宽度和高度在这个范围内。
	 *    例如800*600的原图，thumbMaxWidth=120和thumbMaxHeight=120时，生成的缩略图为120×90
	 * 3. 缩略图类型和原图一致，如果扩展名类型不一致，那么系统会自动在文件名后面加上指定类型扩展名，
	 *    例如origin.jpg生成缩略图dest.bmp时，系统会输入到"dest.bmp.JPEG"文件
	 * </P>
	 * @param originFile：图片必须可读
	 * @param destFile：图片必须可写，如果已经存在就会直接覆盖
	 * @param thumbMaxWidth:缩略图最大宽度
	 * @param thumbMaxHeight:缩略图最大高度
	 * @return true-successfully generate thumb nail; 
	 * 		   false-failed to generate thumb nail
	 */
	public static final boolean generateThumb(final File originFile, final File destFile, 
			final int thumbMaxWidth, final int thumbMaxHeight)
	{
		if(!isParamObjectsNotNull(originFile, destFile) || !isThumbSizeValid(thumbMaxWidth, thumbMaxHeight)){
			logger.error("Fail to generateThumb: error params!");
			return false;
		}
		try{
			Thumbnails.of(originFile).size(thumbMaxWidth, thumbMaxHeight).toFile(destFile);
			return true;
		}catch(Exception ex){
			logger.error("Fail to generateThumb: ex=" + ex.getMessage(), ex);
			return false;
		}
	}

	/********************
	 * <P>和上述接口类似， 只是指定了目标图片文件的类型，需要保证目标文件的扩展名和指定destFileType参数是同一图片类型</P>
	 * @param originFile
	 * @param destFile
	 * @param destFileType
	 * @param thumbMaxWidth
	 * @param thumbMaxHeight
	 * @return
	 */
	public static final boolean generateThumb(final File originFile, final File destFile, final ImageType destFileType,
			final int thumbMaxWidth, final int thumbMaxHeight)
	{
		if(!isParamObjectsNotNull(originFile, destFile, destFileType) || !isThumbSizeValid(thumbMaxWidth, thumbMaxHeight)){
			logger.error("Fail to generateThumb: error params!");
			return false;
		}
		try{
			Thumbnails.of(originFile).size(thumbMaxWidth, thumbMaxHeight).outputFormat(destFileType.extention).toFile(destFile);
			return true;
		}catch(Exception ex){
			logger.error("Fail to generateThumb: ex=" + ex.getMessage(), ex);
			return false;
		}
	}
	
	/*************************
	 * <P>对参数originImageInputStream指定的图片进行等比压缩，压缩后的图片会被保存到destImageOutputStream, 注意:
	 * 1. thumbMaxWidth,thumbMaxHeight是缩略图的最大高度和宽度，由于图片是等比压缩，
	 *    生成的缩略图在等比压缩时，会保证宽度和高度在这个范围内。
	 *    例如800*600的原图，thumbMaxWidth=120和thumbMaxHeight=120时，生成的缩略图为120×90
	 * 2. 缩略图类型和原图一致
	 * 3. 实例:
	 * 	FileInputStream in = new FileInputStream(new File("D:/TDDOWNLOAD/origin.jpg"));
	 * 	FileOutputStream out = new FileOutputStream(new File("D:/TDDOWNLOAD/1.jpg"));
	 * 	ThumbnailUtil.generateThumb(in, out, 100, 100);   
	 * </P>
	 * @param originImageInputStream：图片流必须可读
	 * @param destImageOutputStream：图片流必须可写，如果是FileOutputStream并且文件已经存在就会直接覆盖，不存在则新建文件
	 * @param thumbMaxWidth:缩略图最大宽度
	 * @param thumbMaxHeight:缩略图最大高度
	 * @return true-successfully generate thumb nail; 
	 * 		   false-failed to generate thumb nail
	 */
	public static final boolean generateThumb(InputStream originImageInputStream, OutputStream destImageOutputStream, 
			final int thumbMaxWidth, final int thumbMaxHeight)
	{
		if(!isParamObjectsNotNull(originImageInputStream, destImageOutputStream) || !isThumbSizeValid(thumbMaxWidth, thumbMaxHeight)){
			logger.error("Fail to generateThumb: error params!");
			return false;
		}
		try{
			Thumbnails.of(originImageInputStream).size(thumbMaxWidth, thumbMaxHeight).toOutputStream(destImageOutputStream);
			return true;
		}catch(Exception ex){
			logger.error("Fail to generateThumb: ex=" + ex.getMessage(), ex);
			return false;
		}
	}	
	
	/****************
	 * 旋转图片
	 * @param originImageInputStream
	 * @param destImageOutputStream
	 * @param clockWiseDegree
	 * @return
	 */
	public static final boolean rotateThumb(InputStream originImageInputStream, OutputStream destImageOutputStream, final int clockWiseDegree)
	{
		if(!isParamObjectsNotNull(originImageInputStream, destImageOutputStream) || clockWiseDegree < 1){
			logger.error("Fail to rotateThumb: error params!");
			return false;
		}
		try{
			Thumbnails.of(originImageInputStream).scale(1f).rotate(clockWiseDegree).toOutputStream(destImageOutputStream);
			return true;
		}catch(Exception ex){
			logger.error("Fail to rotateThumb: ex=" + ex.getMessage(), ex);
			return false;
		}
	}	

	public static final byte[] rotateThumb(byte[] imageData, final int clockWiseDegree)
	{
		if(ArrayUtils.isEmpty(imageData) || clockWiseDegree < 1){
			logger.error("Fail to rotateThumb: error params!");
			return null;
		}
		try{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			Thumbnails.of(new ByteArrayInputStream(imageData)).scale(1f).rotate(clockWiseDegree).toOutputStream(out);
			return out.toByteArray();
		}catch(Exception ex){
			logger.error("Fail to rotateThumb: ex=" + ex.getMessage(), ex);
			return null;
		}
	}	

	/**********************
	 * <P>和上述接口类似， 只是指定了目标图片文件的类型，需要保证目标文件的扩展名和指定destFileType参数是同一图片类型</P>
	 * @param originImageInputStream
	 * @param destImageOutputStream
	 * @param destFileType
	 * @param thumbMaxWidth
	 * @param thumbMaxHeight
	 * @return
	 */
	public static final boolean generateThumb(InputStream originImageInputStream, OutputStream destImageOutputStream, 
			final ImageType destFileType, final int thumbMaxWidth, final int thumbMaxHeight)
	{
		if(!isParamObjectsNotNull(originImageInputStream, destImageOutputStream, destFileType) || !isThumbSizeValid(thumbMaxWidth, thumbMaxHeight)){
			logger.error("Fail to generateThumb: error params!");
			return false;
		}
		try{
			Thumbnails.of(originImageInputStream).size(thumbMaxWidth, thumbMaxHeight).outputFormat(destFileType.extention).toOutputStream(destImageOutputStream);
			return true;
		}catch(Exception ex){
			logger.error("Fail to generateThumb: ex=" + ex.getMessage(), ex);
			return false;
		}
	}	
	
	/*************************
	 * <P>对参数originBufferedImage指定的图片进行等比压缩，返回压缩后的图片, 注意:
	 * 1. thumbMaxWidth,thumbMaxHeight是缩略图的最大高度和宽度，由于图片是等比压缩，
	 *    生成的缩略图在等比压缩时，会保证宽度和高度在这个范围内。
	 *    例如800*600的原图，thumbMaxWidth=120和thumbMaxHeight=120时，生成的缩略图为120×90
	 * 2. 缩略图类型和原图一致
	 * 3. 实例:
	 * 	BufferedImage originalImage = ImageIO.read(new File("D:/TDDOWNLOAD/origin.jpg"));
	 * 	BufferedImage buf = ThumbnailUtil.generateThumb(originalImage, ImageType.BMP, 20, 20);
	 * 	ImageIO.write(buf, "bmp", new FileOutputStream(new File("D:/TDDOWNLOAD/1.bmp")));	
	 * </P>
	 * @param originBufferedImage：图片流必须可读
	 * @param thumbMaxWidth:缩略图最大宽度
	 * @param thumbMaxHeight:缩略图最大高度
	 * @return BufferedImage-successfully generate thumb nail; 
	 * 		   null-failed to generate thumb nail
	 */
	public static final BufferedImage generateThumb(BufferedImage originBufferedImage, final int thumbMaxWidth, final int thumbMaxHeight)
	{
		if(!isParamObjectsNotNull(originBufferedImage) || !isThumbSizeValid(thumbMaxWidth, thumbMaxHeight)){
			logger.error("Fail to generateThumb: error params!");
			return null;
		}
		try{
			return Thumbnails.of(originBufferedImage).size(thumbMaxWidth, thumbMaxHeight).asBufferedImage();
		}catch(Exception ex){
			logger.error("Fail to generateThumb: ex=" + ex.getMessage(), ex);
			return null;
		}
	}	

	/**********************
	 * <P>和上述接口类似， 只是指定了目标图片文件的类型，需要保证目标文件的扩展名和指定destFileType参数是同一图片类型</P>
	 * @param originBufferedImage
	 * @param destFileType
	 * @param thumbMaxWidth
	 * @param thumbMaxHeight
	 * @return
	 */
	public static final BufferedImage generateThumb(BufferedImage originBufferedImage, final ImageType destFileType, 
			final int thumbMaxWidth, final int thumbMaxHeight)
	{
		if(!isParamObjectsNotNull(originBufferedImage, destFileType) || !isThumbSizeValid(thumbMaxWidth, thumbMaxHeight)){
			logger.error("Fail to generateThumb: error params!");
			return null;
		}
		try{
			return Thumbnails.of(originBufferedImage).size(thumbMaxWidth, thumbMaxHeight).outputFormat(destFileType.extention).asBufferedImage();
		}catch(Exception ex){
			logger.error("Fail to generateThumb: ex=" + ex.getMessage(), ex);
			return null;
		}
	}		
	
	/*****************************
	 * <P>对参数originImageInputStream指定的图片进行裁剪，裁剪后的图片会被保存到destImageOutputStream, 注意:
	 * 1. width,height是裁剪的宽度和高度，也是最终图片的宽度和高度，高度或宽度超过超过原图的话，就采取原图的高度或宽度。
	 * 2. 实例:
	 * 	FileInputStream in = new FileInputStream(new File("D:/TDDOWNLOAD/origin.jpg"));
	 * 	FileOutputStream out = new FileOutputStream(new File("D:/TDDOWNLOAD/1.jpg"));
	 * 	ThumbnailUtil.cutImage(in, out, ImageCutPosition.TOP_LEFT, 200, 200);
	 * </P>
	 * @param originImageInputStream：图片流必须可读
	 * @param destImageOutputStream：图片流必须可写，如果是FileOutputStream并且文件已经存在就会直接覆盖，不存在则新建文件
	 * @param position：: 裁剪出来的图片相对于原图的位置
	 * @param width: 裁剪出来的图片宽度
	 * @param height: 裁剪出来的图片高度
	 * @return true-successfully cut image; 
	 * 		   false-failed to cut image; 
	 */
	public static final boolean cutImage(InputStream originImageInputStream, OutputStream destImageOutputStream, final ImageCutPosition imageCutPosition, 
			final int width, final int height)
	{
		if(!isParamObjectsNotNull(originImageInputStream, destImageOutputStream, imageCutPosition) || !isThumbSizeValid(width, height)){
			logger.error("Fail to cutImage: error params!");
			return false;
		}		
		try{
			try{
				Thumbnails.of(originImageInputStream).sourceRegion(imageCutPosition.position, width, height).size(width, height).
					toOutputStream(destImageOutputStream);
				return true;
			}catch(IIOException e){
				originImageInputStream.reset();
				final BufferedImage bi = ImageCMYKUtil.readCMYKImage(originImageInputStream);
				Thumbnails.of(bi).sourceRegion(imageCutPosition.position, width, height).size(width, height).outputFormat(ImageType.JPEG.extention).toOutputStream(destImageOutputStream);	
				return true;			
			}
		} catch(Exception ex){
			logger.error("Fail to cutImage: ex=" + ex.getMessage(), ex);
			return false;
		}
	}
	/**
	 * 自定义起始坐标裁剪
	 * @param originImageInputStream
	 * @param destImageOutputStream
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final boolean cutImage(final File originFile, final File destFile,
			final int x, final int y, final int width, final int height)
	{
		if(!isParamObjectsNotNull(originFile, destFile) || !isThumbSizeValid(width, height)){
			logger.error("Fail to cutImage: error params!");
			return false;
		}		
		try{
			Thumbnails.of(originFile).sourceRegion(x, y, width, height).size(width, height).toFile(destFile);
			return true;
		}catch(Exception ex){
			logger.error("Fail to cutImage: ex=" + ex.getMessage(), ex);
			return false;
		}
	}
	
	/**
	 * @Title: cutImage
	 * @Description: 将图片自定义起始坐标裁剪自定义宽高
	 * @param inputStream
	 * @param outpuInputStream
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 * @throws
	 */
	public static final boolean cutImage(final InputStream inputStream, final OutputStream outpuInputStream,
			final int x, final int y, final int width, final int height)
	{
		if(!isParamObjectsNotNull(inputStream, inputStream) || !isThumbSizeValid(width, height)){
			logger.error("Fail to cutImage: error params!");
			return false;
		}	
		try{
			try{
				if(x <= 1 && y <= 1)
				{
					final BufferedImage image = ThumbnailUtil.readBufferedImage(inputStream);
					inputStream.reset();
					if(width >= (image.getWidth()-1) && height >= (image.getHeight()-1)) {
						IOUtil.outputFromInputStream(inputStream, outpuInputStream);
					}else{
						Thumbnails.of(inputStream).sourceRegion(x, y, width, height).size(width, height).toOutputStream(outpuInputStream);
					}
					return true;
				}else{
					Thumbnails.of(inputStream).sourceRegion(x, y, width, height).size(width, height).toOutputStream(outpuInputStream);
				}
				return true;
			}catch(IIOException e){
				inputStream.reset();
				final BufferedImage bi = ImageCMYKUtil.readCMYKImage(inputStream);
				Thumbnails.of(bi).sourceRegion(x, y, width, height).size(width, height).outputFormat(ImageType.JPEG.extention).toOutputStream(outpuInputStream);	
				return true;			
			}
		} catch(Exception ex){			
			logger.error("Fail to cutImage: ex=" + ex.getMessage(), ex);
			return false;
		}
	}
	
	/****************
	 * 采用七牛的上传，url裁剪，下载并再次上传，非常耗性能，尽量不要调用
	 * @param inputStream
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final String cutImageUsingQiniu(final InputStream inputStream, final String uploadKey, 
			final int x, final int y, final int width, final int height)
	{
		String url = null;;
		try {
			url = QiNiuUtil.uploadFile2QiNiu(QiNiuSpace.SaihuitongImage, uploadKey, 
					IOUtil.input2byte(inputStream), 3);
		} catch (Exception e) {
			logger.error("Fail to cutImageUsingQiniu:" + uploadKey, e);
		}
		if(url == null){
			return null;
		}
		final byte[] newImages = IOUtil.readFromHttpURL(null, false, 
				url + "?imageMogr2/crop/!" +width + "x" + height + "a" + x + "a" + y, 3);
		if(ArrayUtils.isEmpty(newImages)){
			return null;
		}
		return QiNiuUtil.uploadFile2QiNiu(QiNiuSpace.SaihuitongImage, uploadKey, newImages, 3);
	}

	/*****************************
	 * <P>对参数originFile指定的图片进行裁剪，裁剪后的图片会被保存到destFile, 注意:
	 * 1. width,height是裁剪的宽度和高度，也是最终图片的宽度和高度，高度或宽度超过超过原图的话，就采取原图的高度或宽度。
	 * 2. 实例:
	 * 	File in = new File("D:/TDDOWNLOAD/origin.jpg");
	 * 	File out = new File("D:/TDDOWNLOAD/1.jpg");
	 * 	ThumbnailUtil.cutImage(in, out, ImageCutPosition.TOP_LEFT, 200, 200);
	 * </P>
	 * @param originFile：图片流必须可读
	 * @param destFile：图片流必须可写，如果是destFile并且文件已经存在就会直接覆盖，不存在则新建文件
	 * @param position：: 裁剪出来的图片相对于原图的位置
	 * @param width: 裁剪出来的图片宽度
	 * @param height: 裁剪出来的图片高度
	 * @return true-successfully cut image; 
	 * 		   false-failed to cut image; 
	 */
	public static final boolean cutImage(final File originFile, final File destFile, final ImageCutPosition imageCutPosition, 
			final int width, final int height)
	{
		if(!isParamObjectsNotNull(originFile, destFile, imageCutPosition) || !isThumbSizeValid(width, height)){
			logger.error("Fail to cutImage: error params!");
			return false;
		}		
		try{
			Thumbnails.of(originFile).sourceRegion(imageCutPosition.position, width, height).size(width, height).toFile(destFile);
			return true;
		}catch(Exception ex){
			logger.error("Fail to cutImage: ex=" + ex.getMessage(), ex);
			return false;
		}
	}
	
	/*****************************
	 * <P>对参数originBufferedImage指定的图片进行裁剪，裁剪后的图片会被保存到返回结果中, 注意:
	 * 1. width,height是裁剪的宽度和高度，也是最终图片的宽度和高度，高度或宽度超过超过原图的话，就采取原图的高度或宽度。
	 * 2. 实例:
	 * 	BufferedImage originalImage = ImageIO.read(new File("D:/TDDOWNLOAD/origin.jpg"));
	 * 	BufferedImage buf = ThumbnailUtil.cutImage(originalImage, ImageCutPosition.TOP_LEFT, 200, 200);
	 * 	ImageIO.write(buf, "jpg", new FileOutputStream(new File("D:/TDDOWNLOAD/1.jpg")));	
	 * </P>
	 * @param originBufferedImage：图片流必须可读
	 * @param position：: 裁剪出来的图片相对于原图的位置
	 * @param width: 裁剪出来的图片宽度
	 * @param height: 裁剪出来的图片高度
	 * @return BufferedImage-successfully cut image; 
	 * 		   null-failed to cut image
	 */
	public static final BufferedImage cutImage(BufferedImage originBufferedImage, final ImageCutPosition imageCutPosition, 
			final int width, final int height)
	{
		if(!isParamObjectsNotNull(originBufferedImage, imageCutPosition) || !isThumbSizeValid(width, height)){
			logger.error("Fail to cutImage: error params!");
			return null;
		}		
		try{
			return Thumbnails.of(originBufferedImage).sourceRegion(imageCutPosition.position, width, height)
				.size(width, height).asBufferedImage();
		}catch(Exception ex){
			logger.error("Fail to cutImage: ex=" + ex.getMessage(), ex);
			return null;
		}
	}
	
	/******************
	 * 生成icon文件
	 * @param in
	 * @param width
	 * @param height
	 * @return null if failed
	 */
	public static final void generateAndOutputIconFile(final String url, final ByteArrayOutputStream out)
	{
		 try {
			 final BufferedImage bufferedImage = ThumbnailUtil.readBufferedImage(
					 IOUtil.readFromHttpURL(null, false, url, 2)); 	
			 net.sf.image4j.codec.ico.ICOEncoder.write(bufferedImage, out);
			 out.flush();
        } catch (Exception e) {
            logger.error("Fail to generate icon, err=" + e.getMessage(), e);
        } 		
	}
	
	 /**
	   * 图片缩放
	   * @param filePath 图片路径
	   * @param height 高度
	   * @param width 宽度
	   * @param bb 比例不对时是否需要补白
	 */
	public static ByteArrayOutputStream resizeImageToJPGByProcessPNG(final InputStream in, int height, int width, boolean bb) throws Exception
	{
		final ByteArrayOutputStream out = ThumbnailUtil.resizeImageToJPGByProcessPNG(ThumbnailUtil.readBufferedImage(in), height, width, bb);
		IOUtil.closeStreamWithoutException(in, null);
		return out;
	}
	
	public static ByteArrayOutputStream resizeImageToJPGByProcessPNG(final BufferedImage bi, final int height, final int width, boolean bb) throws Exception
	{
		double ratio = 0; //缩放比例    
	    Image itemp = bi; //bi.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);   //做下拉伸
	    //计算比例   进行压缩
	    if ((bi.getHeight() > height) || (bi.getWidth() > width)) 
	    {   
	       if (bi.getHeight() > height) {   
	     	   ratio = (new Integer(height)).doubleValue() / bi.getHeight();   
	       } else {   
	           ratio = (new Integer(width)).doubleValue() / bi.getWidth();   
	       }   
	       AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);   
	       itemp = op.filter(bi, null);   
	    }   
	    if (bb) { //不足的话补白
	      	final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);   
	        final Graphics2D g = image.createGraphics();   
	        g.setColor(Color.white);   
	        g.fillRect(0, 0, width, height);  
	        final int itempWidth = itemp.getWidth(null);
	        final int itempHeight = itemp.getHeight(null);
	        if (width <= itempWidth)  {
	         	 g.drawImage(itemp, 0, (height - itempHeight) / 2, itempWidth, itempHeight, Color.white, null);   
	        }else if(height <= itempHeight){
	             g.drawImage(itemp, (width - itempWidth) / 2, 0, itempWidth, itempHeight, Color.white, null);
	        }else{
	        	g.drawImage(itemp, (width-itempWidth) / 2, (height-itempHeight) / 2, itempWidth, itempHeight, Color.white, null); 
	        }
	        g.dispose();   
	        itemp = image;   
	    }
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    if(itemp instanceof BufferedImage){
	    	ImageIO.write((BufferedImage) itemp, (bb ? ImageType.PNG.extention : ImageType.JPG.extention), out);
	    }else{	    	
	    	BufferedImage tmpBI = new BufferedImage(width, height, (bb ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_INT_RGB));
	    	final Graphics2D tmpBIG2d = tmpBI.createGraphics();
	    	tmpBIG2d.drawImage(Toolkit.getDefaultToolkit().createImage(itemp.getSource()), 0, 0, width, height, Color.white, null);
	    	ImageIO.write(tmpBI, (bb ? ImageType.PNG.extention : ImageType.JPG.extention), out);
	    	tmpBIG2d.dispose();
	    }
	    return out;
	}
	
	/*******************
	 * 生成微信使用的微信logo,如果尺寸足够返回null
	 * @param in
	 * @param height
	 * @param width
	 * @return
	 * @throws Exception
	 */
	public static final ByteArrayOutputStream generateWeixinIconByMinSize(final InputStream in, final int minHeightAndWidth) throws Exception
	{
		final BufferedImage logoImage = ThumbnailUtil.readBufferedImage(in);
		if(Math.abs(logoImage.getWidth() - minHeightAndWidth) <= 20 && Math.abs(logoImage.getHeight() - minHeightAndWidth) <= 20){
			return null;
		}
		return ThumbnailUtil.resizeImageToJPGByProcessPNG(logoImage, minHeightAndWidth, minHeightAndWidth, true); //微信仅仅显示方图， 则搞成方图
		/*
		//先对logo做剪裁
		final int cutLogoHeight = (logoImage.getHeight() >= minHeight) ? minHeight : logoImage.getHeight();
		final int cutLogWidth = (logoImage.getWidth() >= minWidth) ? minWidth : logoImage.getWidth();
		//在大的白图中 画上原来的 logo
	 	final BufferedImage resultImage = new BufferedImage(minWidth, minHeight, BufferedImage.TYPE_INT_RGB);   
        final Graphics2D g = resultImage.createGraphics();   
        g.setColor(Color.white);   
        g.fillRect(0, 0, minWidth, minHeight);   
        g.drawImage(logoImage, (minWidth-cutLogWidth) / 2, (minHeight-cutLogoHeight) / 2, cutLogWidth, cutLogoHeight, Color.white, null); 
        g.dispose();   
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ImageIO.write((BufferedImage) resultImage, ImageType.JPG.extention, out);   
	    return out;*/
	}

	
	/*********************
	 * 将16进制的数字字符转化成10进制的color对象，
	 * @param colorHexDigit 至少6位， 可以以0x或者#开头，忽略大小写
	 * @return
	 */
	public static final Color getColor(String colorHexDigit, final Color defaultColorIfNull)
	{
		colorHexDigit = (colorHexDigit != null) ? colorHexDigit.replaceAll("#", "").trim() : "";
		if(colorHexDigit.length() < 6 && colorHexDigit.length() != 3){
			return defaultColorIfNull;
		}
		final int[] colorValues = new int[]{0,0,0};
		if(colorHexDigit.length() == 3){
			colorValues[0] = MD5SignUtil.convert2Decimal(colorHexDigit.charAt(0)) * 16 
					+ MD5SignUtil.convert2Decimal(colorHexDigit.charAt(0));
			colorValues[1] = MD5SignUtil.convert2Decimal(colorHexDigit.charAt(1)) * 16 
					+ MD5SignUtil.convert2Decimal(colorHexDigit.charAt(1));
			colorValues[2] = MD5SignUtil.convert2Decimal(colorHexDigit.charAt(2)) * 16 
					+ MD5SignUtil.convert2Decimal(colorHexDigit.charAt(2));
		}else{
			colorValues[0] = MD5SignUtil.convert2Decimal(colorHexDigit.charAt(0)) * 16 
					+ MD5SignUtil.convert2Decimal(colorHexDigit.charAt(1));
			colorValues[1] = MD5SignUtil.convert2Decimal(colorHexDigit.charAt(2)) * 16 
					+ MD5SignUtil.convert2Decimal(colorHexDigit.charAt(3));
			colorValues[2] = MD5SignUtil.convert2Decimal(colorHexDigit.charAt(4)) * 16 
					+ MD5SignUtil.convert2Decimal(colorHexDigit.charAt(5));
		}
		return new Color(colorValues[0], colorValues[1], colorValues[2]);
	}
	
	/*****************
	 * 格式化颜色参数值
	 * @param color
	 * @param defaultColor
	 * @return
	 */
	public static final String formatColorValue(String color, final String defaultColor)
	{
		color = (color == null) ? "" : color.replaceAll("(&nbsp;)|(&lt;)|(&gt;)|(&amp;)|(&quot;)|(&reg;)|(&copy;)|(&trade;)|(&ensp;)|(&emsp;)|( )|(#)", "");
		if(color.length() == 6){
			return "#" + color;
		}else if(color.length() == 3){
			return new StringBuilder("#").append(color.charAt(0)).append(color.charAt(0)).append(color.charAt(1))
					.append(color.charAt(1)).append(color.charAt(2)).append(color.charAt(2)).toString();
		}
		return defaultColor;
	}
	
	/***************
	 * 获取最合理的宽度或者高度
	 * @param x
	 * @param imageWidth
	 * @param width
	 * @return
	 */
	public static final int getImageValidWidth(final int x, final int imageWidth, final int width){
		return (x + width > imageWidth) ? (imageWidth - x) : width; 
	}
	
	public static final int getImageValidHeight(final int y, final int imageHeight, final int height){
		return (y + height > imageHeight) ? (imageHeight - y) : height; 
	}
	
	/***************
	 * 获取字体对象
	 * @param fontName
	 * @param isBold
	 * @param fontSize
	 * @return
	 */
	public static final Font getFont(String fontName, final boolean isBold, final int fontSize){
		if(StringUtils.isBlank(fontName)){
			fontName = "宋体";
		}
		return (isBold)? new Font(fontName, Font.BOLD, fontSize) 
			: new Font(fontName, Font.PLAIN, fontSize);// 添加字体的属性设置  
	}
	
	/******************
	 * 读取图片
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static final BufferedImage readBufferedImage(final InputStream in) throws Exception{
		try{
			return ImageIO.read(in);
		}catch(IIOException e){
			in.reset();
			return ImageCMYKUtil.readCMYKImage(in);
		}
	}

	public static final BufferedImage readBufferedImage(final byte[] data) throws Exception{
		try{
			return ImageIO.read(new ByteArrayInputStream(data));
		}catch(IIOException e){
			return ImageCMYKUtil.readCMYKImage(data);
		}
	}
	
	public static final BufferedImage readBufferedImageByCheckCMYK(final InputStream in) throws Exception 
	{
		final byte[] bytes = IOUtil.outputFromInputStream(in, true);
		if(Sanselan.getImageInfo(bytes).getColorType() == ImageInfo.COLOR_TYPE_CMYK){//CMYK
			return ImageCMYKUtil.readCMYKImage(bytes);
		}else{
			return ImageIO.read(in);
		}		
	}
	
	 /**
	  * 添加文字水印
	  * @param targetImg 目标图片路径，如：D://myPictrue//1.jpg
	  * @param waterMarkText 水印文字， 如：中国证券网
	  * @param fontName 字体名称，    如：宋体
	  * @param fontStyle 字体样式，如：粗体和斜体(Font.BOLD|Font.ITALIC)
	  * @param fontSize 字体大小，单位为像素
	  * @param color 字体颜色
	  * @param x 水印文字距离目标图片左侧的偏移量，如果x<0, 则在正中间
	  * @param y 水印文字距离目标图片上侧的偏移量，如果y<0, 则在正中间
	  * @param alpha 透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
	  */
	  public static final InputStream addTextWaterMark(final byte[] sourceImage, final String waterMarkText, final String fontName,
                                                     final boolean isBold, final int fontSize, final Color color, final WatermarkPosition position)
	  {
		  return ThumbnailUtil.addTextWaterMark(new ByteArrayInputStream(sourceImage), waterMarkText, fontName, isBold, fontSize, color, position);
	  }
	
	  public static final InputStream addTextWaterMark(final InputStream sourceImage, final String waterMarkText, final String fontName,
																										 final boolean isBold, int fontSize, final Color color, final WatermarkPosition position)
	  {
		  if(StringUtils.isBlank(waterMarkText) || position == null){
			  return sourceImage;
		  }
		  final ByteArrayOutputStream out = new ByteArrayOutputStream();
		  try {	 
			  BufferedImage image = ThumbnailUtil.readBufferedImage(sourceImage); 	
			  int width = image.getWidth(null);
			  int height = image.getHeight(null);
			  if(width < 400 || height < 200){
				  sourceImage.reset();
				  return sourceImage;
			  }else if(width > PageLimit.TenThousand.limit || height > PageLimit.TenThousand.limit){ //太大图片做压缩，免得outOfMemory
				  image = ThumbnailUtil.generateThumb(image, PageLimit.TenThousand.limit, PageLimit.TenThousand.limit);
				  width = image.getWidth(null);
				  height = image.getHeight(null);
			  }
			  BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			  fontSize = (width > 800) ? (int)(fontSize * 1.0d * width / 800) : fontSize;
			  Graphics2D g = resultImage.createGraphics();
			  g.drawImage(image, 0, 0, width, height, null);
			  g.setFont(ThumbnailUtil.getFont(fontName, isBold, fontSize)); //new Font(fontName, fontStyle, fontSize));
			  g.setColor(color);
			  g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, AlbumConstant.ImageWaterMarkFontAlpha)); //alpha
			  int width_1 = fontSize * ThumbnailUtil.getTextLength(waterMarkText);
			  int height_1 = fontSize;
			  int widthDiff = width - width_1;
			  int heightDiff = height - height_1 - ((width > 800) ? (int)(9d * width / 800) : 9);
			  int x = 0;
			  if (WatermarkPosition.BOTTOM_CENTER == position){
			    x = widthDiff / 2;
        }else if (WatermarkPosition.BOTTOM_LEFT == position){
			    x = (width > 800) ? (int)(9d * width / 800) : 9;
        }else if (WatermarkPosition.BOTTOM_RIGHT == position){
          x = width - width_1 - ((width > 800) ? (int)(9d * width / 800) : 9);
        }else {
			    x = widthDiff / 2; //默认中间
        }
			  final int y = heightDiff;
			  if(x > 0 && y > 0){
				  g.drawString(waterMarkText, x, y + height_1);
			  }
			  g.dispose();
			  ImageIO.write(resultImage, ImageType.JPG.extention, out);
			  return new ByteArrayInputStream(out.toByteArray());
		  } catch (Exception e) {
			 logger.error("Fail to add text water market : " + e.getMessage(), e);			 
			try {
				final BufferedImage image = ThumbnailUtil.readBufferedImage(sourceImage);
				ImageIO.write(image, ImageType.JPG.extention, out);
				return new ByteArrayInputStream(out.toByteArray());
			} catch (Exception e1) {
				return null;
			} 				 
		  }
	  }
  
  /**
   * 添加图片水印
   *
   * @param sourceImage 目标图片
   * @param waterMarkPicture   水印icon
   * @param alpha       透明度
   * @param position    水印位置
   * @return
   */
	  public static final InputStream addPictureWaterMark(final byte[] sourceImage, final byte[] waterMarkPicture, final double alpha,
                                                        WatermarkPosition position){
	    return ThumbnailUtil.addPictureWaterMark(new ByteArrayInputStream(sourceImage), new ByteArrayInputStream(waterMarkPicture),
          alpha, position);
    }
    
    public static final InputStream addPictureWaterMark(final InputStream sourceImage, final InputStream waterMarkPicture, final double alpha,
                                                        WatermarkPosition position){
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
        sourceImage.mark(sourceImage.available());
        BufferedImage srcImage = ThumbnailUtil.readBufferedImage(sourceImage);
        BufferedImage iconImage = ThumbnailUtil.readBufferedImage(waterMarkPicture);
        int srcWidth = srcImage.getWidth(null);
			  int srcHeight = srcImage.getHeight(null);
			  int iconWidth = iconImage.getWidth(null);
			  int iconHeight = iconImage.getHeight(null);
			  if(srcWidth < 400 || srcHeight < 200){
				  sourceImage.reset();
				  return sourceImage;
			  }else if(srcWidth > PageLimit.TenThousand.limit || srcHeight > PageLimit.TenThousand.limit){ //太大图片做压缩，免得outOfMemory
				  srcImage = ThumbnailUtil.generateThumb(srcImage, PageLimit.TenThousand.limit, PageLimit.TenThousand.limit);
				  srcWidth = srcImage.getWidth(null);
				  srcHeight = srcImage.getHeight(null);
			  }
//			  if (iconWidth >= srcWidth && iconHeight >= srcHeight){
//			    if (sourceImage.markSupported()){
//			      sourceImage.reset();
//          }
//          return sourceImage;
//        }
        //水印图片宽度为原图宽度的1/8
        int quireWidth = (int)(srcWidth * 0.125d);
        if (quireWidth < iconWidth){
          double percent = (double) quireWidth / (double) iconWidth;
          iconImage = ThumbnailUtil.generateThumb(iconImage, (int)(iconWidth * percent), (int)(iconHeight * percent));
          if (null != iconImage){
            iconWidth = iconImage.getWidth();
            iconHeight = iconImage.getHeight();
          }else {
            return sourceImage;
          }
        }
        final BufferedImage resultImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(srcImage.getScaledInstance(srcWidth, srcHeight, BufferedImage.TYPE_INT_RGB), 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) alpha));
        int[] pts = ThumbnailUtil.calculateCoordinates(srcWidth, srcHeight, iconWidth, iconHeight, position);
        g.drawImage(iconImage, pts[0], pts[1], null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.dispose();
        ImageIO.write(resultImage, ImageType.JPG.extention, out);
        return new ByteArrayInputStream(out.toByteArray());
      } catch (Exception e) {
        logger.error("Fail to add picture water market : " + e.getMessage(), e);
        try {
          final BufferedImage image = ThumbnailUtil.readBufferedImage(sourceImage);
          ImageIO.write(image, ImageType.JPG.extention, out);
          return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e1) {
          return null;
        }
      }
	  }
  
  /**
   * 计算水印位置  位置包含(底部靠左、底部居中、底部靠右)
   * @param srcWidth
   * @param srcHeight
   * @param iconWidth
   * @param iconHeight
   * @param position
   * @return
   */
	  public static final int[] calculateCoordinates(final int srcWidth, final int srcHeight, final int iconWidth,
                                                   final int iconHeight, WatermarkPosition position){
        int[] pts = new int[2];
        if (null == position){
          return pts;
        }
        if (position == WatermarkPosition.BOTTOM_LEFT){
          pts[0] = 10;
          pts[1] = srcHeight - iconHeight - 10;
        }
        if (position == WatermarkPosition.BOTTOM_CENTER){
          pts[0] = (srcWidth - iconWidth) /2;
          pts[1] = srcHeight - iconHeight - 10;
        }
        if (position == WatermarkPosition.BOTTOM_RIGHT){
          pts[0] = srcWidth - iconWidth - 10;
          pts[1] = srcHeight - iconHeight -10;
        }
        if (pts[0] < 0){
          pts[0] = 0;
        }
        if (pts[1] < 0){
          pts[1] = 0;
        }
        return pts;
    }
	  
	  /**
		* 获取字符长度，一个汉字作为 1 个字符, 一个英文字母作为 0.5 个字符
		* @param text
		* @return 字符长度，如：text="中国",返回 2；text="test",返回 2；text="中国ABC",返回 4.
		*/
	  public static final int getTextLength(String text) {
		  int textLength = text.length();
		  int length = textLength;
		  for (int i = 0; i < textLength; i++) {
			  if (String.valueOf(text.charAt(i)).getBytes().length > 1) {
				  length++;
			  }
		  }
		  return (length % 2 == 0) ? length / 2 : length / 2 + 1;
	  }
	  
	  public static void main(String[] args) throws Exception
	  {
		  huaweiHeiPing("0515_1.jpg");
		  huaweiHeiPing("0515_2.jpg");
		  huaweiHeiPing("0515_3.jpg");
		  huaweiHeiPing("0515_4.jpg");
		  huaweiHeiPing("0515_5.jpg");
		  huaweiHeiPing("0515_6.jpg");
		  huaweiHeiPing("0515_7.jpg");		  
		  
//		  int i = 1;
//		  for(final String f : new String[]{"D:/1.jpg", "D:/1.png"})
//		  {
//			  final BufferedImage bufferedImage = ImageIO.read(new File(f));
//			  final FileOutputStream out = new FileOutputStream(new File("D:/" + (i++) + ".ico"));
//			  net.sf.image4j.codec.ico.ICOEncoder.write(bufferedImage, out);
//			  out.flush();
//			  out.close();
//		  }
	  }
	  private static final void huaweiHeiPing(final String name) throws Exception
	  {
		 final String f = "D:/黑屏照片/" + name;
		 byte[] img = IOUtil.readByteFile(new File(f));
		 new File("D:/"+name+"-1.jpg").createNewFile();
		 IOUtil.outputFromBytes(img, new FileOutputStream(new File("D:/"+name+"-1.jpg")));
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 if(!ThumbnailUtil.cutImage(new ByteArrayInputStream(img), out, 1, 1, 800, 800)){
			return;
		 }
		 img = out.toByteArray();
		 new File("D:/"+name+"-2.jpg").createNewFile();
		 IOUtil.outputFromBytes(img, new FileOutputStream(new File("D:/"+name+"-2.jpg")));
		 ByteArrayInputStream tmp = (ByteArrayInputStream)ThumbnailUtil.addTextWaterMark(img, "helloworld", ScoreFieldFont.Song.name, 
					true, 24, ThumbnailUtil.getColor("0x334455", Color.white), WatermarkPosition.BOTTOM_CENTER);
		 out = new ByteArrayOutputStream();
		 IOUtil.outputFromInputStream(tmp, out);
		 img = out.toByteArray();
		 new File("D:/"+name+"-3.jpg").createNewFile();
		 IOUtil.outputFromBytes(img, new FileOutputStream(new File("D:/"+name+"-3.jpg")));
	  }

}