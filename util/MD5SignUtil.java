package com.sas.core.util;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.EncryptConstant.AesEncryptKey;
import com.sas.core.constant.EncryptConstant.Md5Salt;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TimeConstant.Minutes;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.exception.ServerUnknownException;

/********************
 * MD5的签名类
 * 
 * @author zhuliming
 * 
 */
public final class MD5SignUtil {

	private static final Logger logger = Logger.getLogger(MD5SignUtil.class);
    
	//十六进制小写
	private static final char[] HexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	//十六进制大写
	private static final char[] HexArrayUpperCase = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static final void main(String [] agrs) throws Exception{
		for(int i=0; i<10; i++){
			System.out.println(MD5SignUtil.generateRandomKey("1234567890qazwsxedcrfvtgbyhnujmikolp", 10));
			ThreadUtil.sleepNoException(Miliseconds.OneSecond.miliseconds);
		}
	}
	
	/***************
	 * 将16进制字符转化成十进制
	 * @param hexChar
	 * @return
	 */
	public static final int convert2Decimal(final char hexChar){
		for(int index=0; index<HexArray.length; index++){
			if(HexArray[index] == hexChar || HexArrayUpperCase[index] == hexChar){
				return index;
			}
		}
		return 0;
	}
	
	/****************
	 * 生成随机的密码或加密的key
	 * @param includeChars
	 * @param length
	 * @return
	 */
	public static final String generateRandomKey(final String includeChars, final int length)
	{
		final Random rand = new Random(System.currentTimeMillis()%Miliseconds.OneDay.miliseconds);  
		final StringBuilder sb = new StringBuilder("");
		for(int i=0; i<length; i++){
			final int index = rand.nextInt(includeChars.length());
			sb.append(includeChars.charAt(index));
		}
		return sb.toString();
	}
	
	public static final String generateRandomKey(final String includeChars, final int length, final long seed)
	{
		Random rand = null;
		if(seed > 0){
			rand = new Random(seed);  
		}else{
			rand = new Random(System.currentTimeMillis()%Miliseconds.OneHour.miliseconds );  
		}
		final StringBuilder sb = new StringBuilder("");
		for(int i=0; i<length; i++){
			final int index = rand.nextInt(includeChars.length());
			sb.append(includeChars.charAt(index));
		}
		return sb.toString();
	}
	
	/*************************
	 * 采用二次加盐策略生成消息摘要
	 * 
	 * @param salt
	 *            盐值
	 * @param data
	 *            数据
	 * @return 摘要
	 * @throws Exception
	 *             异常
	 */
	public static final String generateSignature(final Md5Salt md5Salt, final String data, final int maxResultStringLength) {
		final String result = generateSignature(md5Salt, data);
		if(result == null){
			throw new ServerUnknownException("Fail to generateSignature with maxLength");
		}
		if(maxResultStringLength>0 && result.length() > maxResultStringLength){
			return result.substring(0, maxResultStringLength);
		}
		return result;
	}
	
	/*************************
	 * 采用二次加盐策略生成消息摘要
	 * 
	 * @param salt
	 *            盐值
	 * @param data
	 *            数据
	 * @return 摘要
	 * @throws Exception
	 *             异常
	 */
	public static final String generateSignature(final Md5Salt md5Salt, final String data) {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			// 一次MD5
			byte[] dataBytes = md.digest(data.getBytes(Encoding.UTF8.type));
			// 二次MD5
			byte[] siteBytes = md5Salt.salt.getBytes(Encoding.UTF8.type);
			for (int i = 0; i < dataBytes.length && i < siteBytes.length; i++) {
				dataBytes[i] = (byte) (dataBytes[i] ^ siteBytes[i]);
			}
			return bytesToHexStr(md.digest(dataBytes));
		} catch (Exception ex) {
			logger.error("Fail to generate MD5 String: ex=" + ex.getMessage(), ex);
			return null;
		}
	}

	public static final String generateSingleSignature(final String md5Salt, final String data) {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			// 一次MD5
			final byte[] dataBytes = md.digest(data.getBytes(Encoding.UTF8.type));		
			final String newPlainText = md5Salt+bytesToHexStr(dataBytes);
			// 二次MD5
			return bytesToHexStr(md.digest(newPlainText.getBytes(Encoding.UTF8.type)));
		} catch (Exception ex) {
			logger.error("Fail to generate MD5 String: ex=" + ex.getMessage(), ex);
			return null;
		}
	}
	
	/*******************
	 * AES加密
	 * @param text
	 * @param encryptKey
	 * @return
	 * @throws Exception
	 */
	public static String aesEncrypt(final String text, final AesEncryptKey aesEncryptKey) throws Exception{
        final byte[] raw = aesEncryptKey.key.getBytes(Encoding.UTF8.type);
        final SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(text.getBytes(Encoding.UTF8.type));
        return MD5SignUtil.bytesToHexStr(encrypted).toLowerCase();
	}
	
	/*****************
	 * aes加密， 异常时返回空串
	 * @param text
	 * @param aesEncryptKey
	 * @return
	 */
	public static String aesEncryptWithoutException(final String text, final AesEncryptKey aesEncryptKey){
		try{
			return MD5SignUtil.aesEncrypt(text, aesEncryptKey);
		}catch(Exception ex){
			logger.error("Fail to encrypt text of text=" + text + ", key=" + aesEncryptKey.key, ex);
			return "";
		}
	}
	
	/******************
	 * AES 解密
	 * @param encryptedString
	 * @param encryptKey
	 * @return
	 * @throws Exception
	 */
	public static final String aesDecrypt(String encryptedString, final AesEncryptKey aesEncryptKey) throws Exception{
        byte[] raw = aesEncryptKey.key.getBytes(Encoding.UTF8.type);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] bytes = cipher.doFinal(MD5SignUtil.hexStrToBytes(encryptedString));
        return new String(bytes, Encoding.UTF8.type);
	}
	
	/**
	 * 将字节数组转换为16进制字符串的形式.
	 */
	public static final String bytesToHexStr(byte[] bcd) {
		StringBuffer s = new StringBuffer(bcd.length * 2);
		for (int i = 0; i < bcd.length; i++) {
			s.append(HexArray[(bcd[i] >>> 4) & 0x0f]);
			s.append(HexArray[bcd[i] & 0x0f]);
		}

		return s.toString();
	}

	/*****************
	 * 将16进制转成字符串
	 * @param s
	 * @return
	 */
    public static byte[] hexStrToBytes(String s) {
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
    
	/*****************
	 * 根据用户名和密码生成签名摘要，其中expireMinutes为从现在开始计算的有效分钟数
	 * @param plainText   明文
	 * @param expireMinutes  链接有效期（分钟）
	 * @param salt
	 * @param aesEncryptKey
	 * @param maxLength: 如果<=0， 忽略此参数， 否则返回长度不超过该值
	 * @return
	 */	
	public static final String generateSignatureWithinExpireTimes(final String plainText, final Minutes expireMinutes, 
			final Md5Salt salt, final AesEncryptKey aesEncryptKey, final int maxLength)
	{
		if(StringUtils.isBlank(plainText) || salt == null || expireMinutes == null){
			throw new ServerUnknownException("Fail to generateSignatureWithinExpireTimes, error param: plainText="
					+ plainText + ", expireMinutes=" 
					+ expireMinutes + ", salt=" + salt);
		}
		//计算签名
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, expireMinutes.minutes);
		final String expireDateString = TimeUtil.formatDate(cal.getTime(), TimeFormat.YYYYMMDDHHMMSS);	
		try {
			final String encryptString = MD5SignUtil.aesEncrypt(expireDateString+DividerChar.At.chars+plainText, aesEncryptKey);			
			final String result = expireDateString + MD5SignUtil.generateSignature(salt, encryptString);
			if(maxLength < 1){
				return result;
			}else{
				return result.length() <= maxLength ? result : result.substring(0, maxLength);
			}
		} catch (Exception e) {
			logger.error("Fail to generateMD5Signature, err=" + e.getMessage(), e);
			return "";
		}
	}	
	

	/*****************
	 * 判断签名是否正确， 该签名必须由Md5Util.generateSignatureWithinExpireTimes进行生成
	 * @param plainText
	 * @param signature
	 * @param salt
	 * @param aesEncryptKey
	 * @param maxLength: 如果<=0， 忽略此参数， 否则返回长度不超过该值
	 * @return
	 */
	public static final boolean verifySignatureWithinExpireTimes(final String plainText, final String signature,
			final Md5Salt salt, final AesEncryptKey aesEncryptKey, int maxLength){
		if(StringUtils.isBlank(plainText) || StringUtils.isBlank(signature) 
				|| signature.length() <= TimeFormat.YYYYMMDDHHMMSS.format.length()){
			return false;
		}
		final String expireDateString = signature.substring(0, TimeFormat.YYYYMMDDHHMMSS.format.length());
		final long expireDateMiliseconds = TimeUtil.parseDate2Miliseconds(expireDateString, TimeFormat.YYYYMMDDHHMMSS, 0L);
		if(expireDateMiliseconds < System.currentTimeMillis()){
			return false;
		}
		final String md5Signature = signature.substring(TimeFormat.YYYYMMDDHHMMSS.format.length());
		try{
			final String encryptString = MD5SignUtil.aesEncrypt(expireDateString + DividerChar.At.chars + plainText, aesEncryptKey);			
			final String md5 = MD5SignUtil.generateSignature(salt, encryptString);
			if(md5 == null){
				return false;
			}else{
				if(maxLength < 1){
					return md5.equals(md5Signature);
				}else{
					maxLength = maxLength - expireDateString.length();
					if(maxLength > 0 && maxLength < md5.length()){
						return md5.substring(0, maxLength).equals(md5Signature);
					}else{
						return md5.equals(md5Signature);
					}
				}
			}
		}catch(Exception ex){
			logger.error("Fail to verifySignatureWithinExpireTimes:" + signature + ",  plainText=" + plainText 
					+ ", ex="+ex.getMessage(), ex);
			return false;
		}
	}
	
	  /**
	   * 异或加密
	   * @param keyStr 密钥
	   * @param password 待加密字符串
	   * @return
	   * @throws Exception
	   */
	  public static String encryptXOR(String keyStr, String password){
		  int[] snNum = new int[password.length()];
		  StringBuilder result = new StringBuilder();
		  String temp = null;
		  for (int i = 0, j = 0; i < password.length(); i++, j++) {
			  if (j == keyStr.length()){
				  j = 0;
			  }
			  snNum[i] = password.charAt(i) ^ keyStr.charAt(j);
		  }
		  for (int i = 0; i < password.length(); i++) {
			  if (snNum[i] < 10){
				  temp = "00" + snNum[i];
			  }else if (snNum[i] < 100){
				  temp = "0" + snNum[i];
			  }else{
				  temp = String.valueOf(snNum[i]);
			  }
			  result.append(temp);
		  }
		  return result.toString();
	  }
	  
	  /**
	   * 异或解密
	   * @param keyStr 密钥
	   * @param encodedEncryptedPassword 待解密字符串
	   * @return
	   * @throws Exception
	   */
	  public static String decryptXOR(String keyStr, String encodedEncryptedPassword) throws Exception {
		  StringBuilder result = new StringBuilder();
		  char[] snNum = new char[encodedEncryptedPassword.length() / 3];
		  for (int i = 0, j = 0; i < encodedEncryptedPassword.length() / 3; i++, j++) {
			  if (j == keyStr.length()){
				  j = 0;
			  }
			  int n = Integer.parseInt(encodedEncryptedPassword.substring(i * 3, i * 3 + 3));
			  snNum[i] = (char) (n ^ keyStr.charAt(j));
		  }
		  for (int i = 0; i < encodedEncryptedPassword.length() / 3; i++) {
			  result.append(snNum[i]);
		  }
		  return result.toString();
	  }
}