/**
 * @Title: ZxingUtil.java
 * @Package com.sas.core.util
 * @author yunshang_734@163.com
 * @date Jan 24, 2015 1:54:58 PM
 * @version V1.0
 */
package com.sas.core.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.QRConstants.QRType;

/**
 * @ClassName: ZxingUtil
 * @Description: 二维码工具
 * @author yunshang_734@163.com
 * @date Jan 24, 2015 1:54:58 PM
 */
public class QRCodeUtil {
	
	private final static Logger logger = Logger.getLogger(QRCodeUtil.class);
	
	/**
	 * @ClassName: QRCodeSize
	 * @Description: 二维码图片大小
	 * @author yunshang_734@163.com
	 * @date Jan 24, 2015 2:03:24 PM
	 */
	public static enum QRCodeSize{
		Fourty(40, 40),
		Fifty(50, 50),
		Sixty(60, 60),
		Eighty(80, 80),
		Hundred(100, 100),
		HundredTwenty(120, 120),
		HundredFifty(150, 150),
		TwoHundred(200, 200),
		TwoHundredFifty(250, 250),
		ThreeHundred(300, 300),
		FourHundred(400, 400),
		EightHundred(800, 800),
		TwoThousand(2000, 2000);
		public int height;
		public int width;
		private QRCodeSize(int h, int w){
			this.height = h;
			this.width = w;
		}
	}
	
	/**
	 * @Title: encode2InputStream
	 * @Description: 生成二维码图片InputStream
	 * @param content
	 * @param size
	 * @return
	 * @throws
	 */
	public static InputStream encode2InputStream(final String content, final QRCodeSize size){
		try {
			Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
			// 指定纠错等级
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			// 指定编码格式
			hints.put(EncodeHintType.CHARACTER_SET, Encoding.UTF8.type);
			// 指定二維碼白邊
			hints.put(EncodeHintType.MARGIN, 1);
			BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size.width, size.height, hints);
			MatrixToImageConfig config = new MatrixToImageConfig(Color.black.getRGB(), Color.white.getRGB());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(bitMatrix, "jpeg", outputStream, config);
			return new ByteArrayInputStream(outputStream.toByteArray());
		} catch (Exception e) {
			logger.error("Failed to encode2InputStream with Exception : " + e, e);
			return null;
		}
	}
	
	/**
	 * @Title: overlapImage
	 * @Description: 二维码中插入LOGO
	 * @param imageIS
	 * @param logoIS
	 * @throws IOException
	 * @throws
	 */
	public static byte[] overlapImage(InputStream imageIS, BufferedImage bufferedImage){
		//设置logo图片宽度为二维码图片的七分之一
		return QRCodeUtil.overlapImage(imageIS, bufferedImage, 1.0D/6);
    }
	
	public static byte[] overlapImage(InputStream imageIS, BufferedImage bufferedImage, final double logoImageWidthRatio){
		try {
			InputStream imageISCopy = imageIS;
			BufferedImage image = ThumbnailUtil.readBufferedImage(imageISCopy);
	        final int logoWidth = (int)(image.getWidth() * logoImageWidthRatio);   
	        final int logoHeight = (int)(image.getHeight() * logoImageWidthRatio); 
	        final int logoX = (image.getWidth()-logoWidth)/2;   //设置logo图片的位置,这里令其居中
	        final int logoY = (image.getHeight()-logoHeight)/2; //设置logo图片的位置,这里令其居中
	        final Graphics2D graphics = image.createGraphics();
	        graphics.drawImage(bufferedImage, logoX, logoY, logoWidth, logoHeight, null);
	        graphics.dispose();
	        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        final ImageOutputStream imOut = ImageIO.createImageOutputStream(baos);
	        ImageIO.write(image, "jpeg", imOut);
	        imageISCopy = new ByteArrayInputStream(baos.toByteArray());
	        if(imageISCopy != null){
	        	imageIS = imageISCopy;
	        }
	        imageISCopy.close();
		} catch (Exception e) {
			logger.error("Failed to overlapImage with Exception : " + e, e);
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buff = new byte[100];
			int rc = 0;
			while ((rc = imageIS.read(buff, 0, 100)) > 0) {
				baos.write(buff, 0, rc);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			logger.error("Failed to convert qrcode inputstream to bytearray with Exception : " + e, e);
			return null;
		}
    }
	
	/**
	 * @Title: getQRCodeLocalUrl
	 * @Description: 获取二维码地址
	 * @param domain
	 * @param qrType
	 * @param id
	 * @return
	 * @throws
	 */
	public final static String getQRCodeLocalUrl(final String domain, final QRType qrType, final long id){
		return "http://" + domain + "/qrcode?type=" + qrType.type + "&id=" + id;
	}
	
	public final static String getQRCodeLocalUrl(final String domain, final QRType qrType, final long id, final String ext){
		return "http://" + domain + "/qrcode?type=" + qrType.type + "&id=" + id + "&ext=" + ext;
	}
}