package com.sas.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class PinyinUtil {
	
	 private static Logger logger = Logger.getLogger(PinyinUtil.class);
	 
	 /**************
	  * 将地址转成拼音，考虑省， 市等关键字转成英文
	  * @param chineseAddress
	  * @return
	  */
	 public static final String convertAddress2EnglishPinyin(String chineseAddress){
		 if(chineseAddress == null){
			 return "";
		 }
		 chineseAddress = chineseAddress.replace("小区", "Village").replaceAll("街道", "Street").
				 replace("大厦", "Building").replace("广场", "Square")
				 .replaceAll("其他", "other").replace("海外", "overseas")
				 .replace("重庆", "ChongQing").replace("港澳台", "HongKong Macao&Taiwan").replace("香港", "HongKong").replace("澳门", "Macao");
		 final StringBuilder content = new StringBuilder("");
		 StringBuilder roadNumbers = new StringBuilder("");
		 for(final char ch : chineseAddress.toCharArray())
		 {
			 if(ch >= '0' && ch <= '9'){
				 roadNumbers.append(String.valueOf(ch));
			 }else if(ch == ' ' || ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n'){
				 content.append(" ");
			 }else{				 
				 if(ch == '号' || ch == '#'){
					 content.append("Num." + roadNumbers);
				 }else if(ch == '室'){
					 content.append("Room." + roadNumbers);
				 }else{
					 content.append(roadNumbers);
					 switch(ch){
					 case '省': 
						 content.append("Province"); break;
					 case '市': 
						 content.append("City"); break;
					 case '区': 
						 content.append("District"); break;
					 case '镇': 
						 content.append("Town"); break;
					 case '村': 
						 content.append("Village"); break;
					 case '栋': 
					 case '幢': 
						 content.append("Building"); break;
					 case '层': 
						 content.append("Floor"); break;
					 case '街': 
						 content.append("Street"); break;
					 case '路': 
						 content.append("Road"); break;
					 default:
						 content.append(String.valueOf(ch));
					 }
				 }
				 roadNumbers = new StringBuilder("");
			 }
			 
		 }
		 return PinyinUtil.convert2PinYin(content.toString(), true);
	 }
	 
	 /**
	 * Description : 根据汉字获得此汉字的拼音， 结果只有第一个汉字的首字母大写
	 * @return
	 *
	 */
	 public static final String convert2PinYinWithFirstLetterUpperCase(String chineseLetters){
		 String pinyin = getPinYin(chineseLetters, false, false);
		 if(pinyin == null || pinyin.length() < 1){
			 return pinyin;
		 }
		 char firstChar = pinyin.charAt(0);
		 if(firstChar <= 'z' && firstChar >= 'a'){
			 firstChar = (char)(firstChar + ('A' - 'a'));
			 return pinyin.length() < 2 ? String.valueOf(firstChar) : String.valueOf(firstChar) + pinyin.substring(1);
		 }else{
			 return pinyin;
		 }
	 }

	 
	 /**
	 * Description : 根据汉字获得此汉字的拼音
	 * @param needEveryFirstChar2UpperCase: 每个汉字的拼音首字母大写
	 * @return
	 *
	 */
	 public static final String convert2PinYin(String chineseLetters, final boolean needEveryFirstChar2UpperCase){
	    return getPinYin(chineseLetters, false, needEveryFirstChar2UpperCase);
	 }

	/**
	 * Description : 根据汉字获得此汉字的拼音首字母
	 * @param isUpperCase: 是否大写
	 * @return
	 */
	 public static final String convert2PinYinFistChars(String chineseLetters, final boolean isUpperCase){
	    return getPinYin(chineseLetters, true, isUpperCase);
	 }

	 /***********
	  * 转成拼音
	  * @param chineseLetters：含有中文汉字的字符串
	  * @param onlyFirstCharOfEveryLettes： 是否每个汉字只要拼音首字母即可
	  * @param needEveryFirstChar2UpperCase：是否每个汉字的首个拼音要大写
	  * @return
	  */
	 private static final String getPinYin(String chineseLetters, boolean onlyFirstCharOfEveryLettes, final boolean needEveryFirstChar2UpperCase)
	 {
		 if(StringUtils.isBlank(chineseLetters)){
			 return "";
		 }
		 final char[] chars = chineseLetters.toCharArray();

	    // 设置输出格式
	    final HanyuPinyinOutputFormat formatParam = new HanyuPinyinOutputFormat();
	    formatParam.setCaseType(HanyuPinyinCaseType.LOWERCASE);
	    //设置声调格式
	    formatParam.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	    //设置特殊拼音ü的显示格式
	    formatParam.setVCharType(HanyuPinyinVCharType.WITH_V);
	    final StringBuilder py = new StringBuilder("");
	    Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]"); //"^[\u4e00-\u9fa5]{0,128}$");
	    for (int i = 0; i < chars.length; i++)
	    {
	        final Matcher matcher = pattern.matcher(String.valueOf(chars[i]));
	        // 检查是否是汉字,如果不是汉字就不转换
	        if (!matcher.matches()){
	            py.append(chars[i]);
	            continue;
	        }
	        // 对汉字进行转换成拼音
	        try
	        {
	        	final String pinyins = PinyinHelper.toHanyuPinyinStringArray(chars[i], formatParam)[0];
	        	char firstChar = pinyins.charAt(0);
	        	if(needEveryFirstChar2UpperCase && firstChar <= 'z' && firstChar >= 'a'){
    				firstChar = (char)(firstChar + ('A' - 'a'));
    			}
	        	if(pinyins.length() == 1 || onlyFirstCharOfEveryLettes){//就一个字母 
        			py.append(firstChar);
        		}else if(pinyins.length() > 1){//多个字母
        			py.append(String.valueOf(firstChar) + pinyins.substring(1));
        		}else{
        			py.append(chars[i]);
        		}
	        } catch (Exception e){
	            logger.error(chars[i] + " to pinyin error!", e);
	            py.append(chars[i]);
	        }
	    }
	    return py.toString();
	 }
	 
	 /*****************
	  * 将中文国家名转换成英文
	  * @param name
	  * @return
	  */
	 public static final String convertChineseCountryName2English(String name){
		 if(name == null || name.length() < 1){
			 return "";
		 }
		 name = name.trim();
		 final String en = SasMenuActivityAddressTagUtil.getCountryChinese2EnglishNameMap().get(name);
		 return en != null ? en : name; 
	 }
	 
	 public static void main(String[] args)
	 {
	    String test = "重庆";
	    System.out.println(PinyinUtil.convertAddress2EnglishPinyin("杭州市西湖区文二路391号西湖国际大厦D南楼2楼207室"));
	    System.out.println(PinyinUtil.convertAddress2EnglishPinyin("朱立明，86-13758147354，浙江省 杭州市 下城区 东新街道 白石路水印康庭小区1幢东楼610室 ，310052 "));
	    //System.out.println(PinyinUtil.convert2PinYin(test, false));
	   // System.out.println(PinyinUtil.convert2PinYinFistChars(test, true));
	    //System.out.println(PinyinUtil.convert2PinYinFistChars(test, false));
	 }
}
