package com.sas.core.util.alipay;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.Encoding;

/**
 * 支付宝MD5签名处理核心文件，不需要修改
 * @author zhulm
 *
 */
public class AlipayMD5 {
	
	 public static final Logger logger = Logger.getLogger(AlipayMD5.class);
	 
	/**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static final String sign(final String text, final String key, final Encoding charset) {
        return DigestUtils.md5Hex(getContentBytes(text + key, charset));
    }
    
    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param sign 签名结果
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static final boolean verify(String text, String sign, String key, final Encoding charset) {
    	text = text + key;
    	final String mysign = DigestUtils.md5Hex(getContentBytes(text, charset));
    	if(mysign.equals(sign)) {
    		return true;
    	} else {
    		return false;
    	}
    }

    /**
     * 获取指定字符集的字节数组
     * @param content
     * @param charset
     * @return
     */
    public static final byte[] getContentBytes(String content, final Encoding charset) {
        if (charset == null) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset.type);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

}