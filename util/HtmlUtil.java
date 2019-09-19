package com.sas.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.ThirdPartConstant.QiNiuImageCompress;
import com.sas.core.constant.ThirdPartConstant.QiNiuSpace;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.util.ThumbnailUtil.ImageSizeType;
import com.sas.core.util.keywordsearch.SearchAlgorithm;
import com.sas.core.util.meta.VideoUtil;

/************************
 * 富文本的util类
 * @author zhuliming
 *
 */
public class HtmlUtil {
	
	private static final Logger logger = Logger.getLogger(HtmlUtil.class);
	
	// 获取img标签正则
	private static final String IMGURL_REG = "<(IMG|img)[^>]*?\\s+(src|data.{0,20}-src)\\s*=\\s*['\"]?([^'\">]+)(['\"]|\\s)+[^>]*>";
	private static final String IMGURL_REG_DATA_SRC = "data.{0,20}-src\\s*=\\s*['\"]?([^'\">]+)(['\"]|\\s)+";
	
	private static final String SAIHUITONG_VIDEO_REG = "[\"'= ]+(([/:]|[a-z]|[A-Z])+(v\\.saihuitong\\.com|v2\\.saihuitong\\.com)([/\\.]|[a-z]|[A-Z]|[0-9])+)";
	
	private static final String SAIHUITONG_ATTACHMENT_REG = "[\"'= ]+(([/:]|[a-z]|[A-Z])+f\\.saihuitong\\.com([/\\.]|[a-z]|[A-Z]|[0-9])+)";

	//escapes
	public static final String EecapeReg = "(&nbsp;)|(&lt;)|(&gt;)|(&amp;)|(&quot;)|(&reg;)|(&copy;)|(&trade;)|(&ensp;)|(&emsp;)";
    
	public static String HTMLWhiteSpaceReg = "(\\t)+|(\\r)+|(\\n)+|(&nbsp;)+|(&lt;)+|(&gt;)+|(&amp;)+|(&quot;)+|(&reg;)+|(&copy;)+|(&trade;)+|(&ensp;)+|(&emsp;)+|(&#[0123456789]{1,3};)+|(<\\?xml:namespace[^>]{1,100}>)+|(<!\\-\\-[^>]{1,100}\\-\\->)+";
	
	public static String WhiteSpaceReg = "(\\t)+|(\\r)+|(\\n)+|(&nbsp;)+|( )+|(　)+|(&lt;)+|(&gt;)+|(&amp;)+|(&quot;)+|(&reg;)+|(&copy;)+|(&trade;)+|(&ensp;)+|(&emsp;)+|(&#[0123456789]{1,3};)+|(<\\?xml:namespace[^>]{1,100}>)+|(<!\\-\\-[^>]{1,100}\\-\\->)+|(�)+";

	public static String WhiteSpaceRegWithoutBlanks = "(\\t)+|(\\r)+|(\\n)+|(&nbsp;)+|(&lt;)+|(&gt;)+|(&amp;)+|(&quot;)+|(&reg;)+|(&copy;)+|(&trade;)+|(&ensp;)+|(&emsp;)+|(&#[0123456789]{1,3};)+|(<\\?xml:namespace[^>]{1,100}>)+|(<!\\-\\-[^>]{1,100}\\-\\->)+|(�)+";

	//判断图片url是否有效的DFA
	private static SearchAlgorithm validImageURLPatternSearchAlgorithm = null;
	private static SearchAlgorithm inValidImageURLPatternSearchAlgorithm = null;
	
	private static final Map<String, String> chineseDigitalLetterMap = new HashMap<String, String>();
	private static final Map<String, Character> unescapeMap = new HashMap<String, Character>();	
	static {
		unescapeMap.put("&lt;", '<');			unescapeMap.put("&gt;", '>');
		unescapeMap.put("&amp;", '&');			unescapeMap.put("&quot;", '"');
		unescapeMap.put("&agrave;", '\u00E0');	unescapeMap.put("&Agrave;", '\u00C0');
		unescapeMap.put("&acirc;", '\u00E2');	unescapeMap.put("&Acirc;", '\u00C2');
		unescapeMap.put("&auml;", '\u00E4');	unescapeMap.put("&Auml;", '\u00C4');
		unescapeMap.put("&aring;", '\u00E5');	unescapeMap.put("&Aring;", '\u00C5');
		unescapeMap.put("&aelig;", '\u00E6');	unescapeMap.put("&AElig;", '\u00C6');
		unescapeMap.put("&ccedil;", '\u00E7');	unescapeMap.put("&Ccedil;", '\u00C7');
		unescapeMap.put("&eacute;", '\u00E9');	unescapeMap.put("&Eacute;", '\u00C9');
		unescapeMap.put("&egrave;", '\u00E8');	unescapeMap.put("&Egrave;", '\u00C8');
		unescapeMap.put("&ecirc;", '\u00EA');	unescapeMap.put("&Ecirc;", '\u00CA');
		unescapeMap.put("&euml;", '\u00EB');	unescapeMap.put("&Euml;", '\u00CB');
		unescapeMap.put("&iuml;", '\u00EF');	unescapeMap.put("&Iuml;", '\u00CF');
		unescapeMap.put("&ocirc;", '\u00F4');	unescapeMap.put("&Ocirc;", '\u00D4');
		unescapeMap.put("&ouml;", '\u00F6');	unescapeMap.put("&Ouml;", '\u00D6');
		unescapeMap.put("&oslash;", '\u00F8');	unescapeMap.put("&Oslash;", '\u00D8');
		unescapeMap.put("&szlig;", '\u00DF');	unescapeMap.put("&ugrave;", '\u00F9');
		unescapeMap.put("&Ugrave;", '\u00D9');	unescapeMap.put("&ucirc;", '\u00FB');
		unescapeMap.put("&Ucirc;", '\u00DB');	unescapeMap.put("&uuml;", '\u00FC');
		unescapeMap.put("&Uuml;", '\u00DC');	unescapeMap.put("&reg;", '\u00AE');
		unescapeMap.put("&copy;", '\u00A9');	unescapeMap.put("&euro;", '\u20AC');
		unescapeMap.put("&nbsp;", ' ');
		//圆角半角
		chineseDigitalLetterMap.put("０", "0");	chineseDigitalLetterMap.put("１", "1");
		chineseDigitalLetterMap.put("２", "2");	chineseDigitalLetterMap.put("３", "3");
		chineseDigitalLetterMap.put("４", "4");	chineseDigitalLetterMap.put("５", "5");
		chineseDigitalLetterMap.put("６", "6");	chineseDigitalLetterMap.put("７", "7");
		chineseDigitalLetterMap.put("８", "8");	chineseDigitalLetterMap.put("９", "9");
		chineseDigitalLetterMap.put("ａ", "a");	chineseDigitalLetterMap.put("Ａ", "A");
		chineseDigitalLetterMap.put("ｂ", "b");	chineseDigitalLetterMap.put("Ｂ", "B");
		chineseDigitalLetterMap.put("ｃ", "c");	chineseDigitalLetterMap.put("Ｃ", "C");
		chineseDigitalLetterMap.put("ｄ", "d");	chineseDigitalLetterMap.put("Ｄ", "D");
		chineseDigitalLetterMap.put("ｅ", "e");	chineseDigitalLetterMap.put("Ｅ", "E");
		chineseDigitalLetterMap.put("ｆ", "f");	chineseDigitalLetterMap.put("Ｆ", "F");
		chineseDigitalLetterMap.put("ｇ", "g");	chineseDigitalLetterMap.put("Ｇ", "G");
		chineseDigitalLetterMap.put("ｈ", "h");	chineseDigitalLetterMap.put("Ｈ", "H");
		chineseDigitalLetterMap.put("ｉ", "i");	chineseDigitalLetterMap.put("Ｉ", "I");
		chineseDigitalLetterMap.put("ｊ", "j");	chineseDigitalLetterMap.put("Ｊ", "J");
		chineseDigitalLetterMap.put("ｋ", "k");	chineseDigitalLetterMap.put("Ｋ", "K");
		chineseDigitalLetterMap.put("ｌ", "l");	chineseDigitalLetterMap.put("Ｌ", "L");
		chineseDigitalLetterMap.put("ｍ", "m");	chineseDigitalLetterMap.put("Ｍ", "M");
		chineseDigitalLetterMap.put("ｎ", "n");	chineseDigitalLetterMap.put("Ｎ", "N");
		chineseDigitalLetterMap.put("ｏ", "o");	chineseDigitalLetterMap.put("Ｏ", "O");
		chineseDigitalLetterMap.put("ｐ", "p");	chineseDigitalLetterMap.put("Ｐ", "P");
		chineseDigitalLetterMap.put("ｑ", "q");	chineseDigitalLetterMap.put("Ｑ", "Q");
		chineseDigitalLetterMap.put("ｒ", "r");	chineseDigitalLetterMap.put("Ｒ", "R");
		chineseDigitalLetterMap.put("ｓ", "s");	chineseDigitalLetterMap.put("Ｓ", "S");
		chineseDigitalLetterMap.put("ｔ", "t");	chineseDigitalLetterMap.put("Ｔ", "T");
		chineseDigitalLetterMap.put("ｕ", "u");	chineseDigitalLetterMap.put("Ｕ", "U");
		chineseDigitalLetterMap.put("ｖ", "v");	chineseDigitalLetterMap.put("Ｖ", "V");
		chineseDigitalLetterMap.put("ｗ", "w");	chineseDigitalLetterMap.put("Ｗ", "W");
		chineseDigitalLetterMap.put("ｘ", "x");	chineseDigitalLetterMap.put("Ｘ", "X");
		chineseDigitalLetterMap.put("ｙ", "y");	chineseDigitalLetterMap.put("Ｙ", "Y");
		chineseDigitalLetterMap.put("ｚ", "z");	chineseDigitalLetterMap.put("Ｚ", "Z");
		//UTF8 SPACE : http://www.cnblogs.com/mingmingruyuedlut/archive/2012/07/04/2575180.html
		try{
			final String utf8Space = new String(new byte[]{(byte)0xc2, (byte)0xa0}, Encoding.UTF8.type);
			HTMLWhiteSpaceReg = HTMLWhiteSpaceReg + "|(" + utf8Space + ")+";
			WhiteSpaceReg = WhiteSpaceReg + "|(" + utf8Space + ")+";
		}catch(Throwable ex){
			logger.error("Fail to init HTMLWhiteSpaceReg&WhiteSpaceReg : " + ex.getMessage(), ex);
		}
	}

	/******************
	 * 替换图片路径， 设置为lazy load
	 * @param content
	 * @return
	 */
	public static final String processImages2LazyLoad(final String content){
		if (!StringUtils.isBlank(content)) {
			try {
				StringBuilder sb = new StringBuilder();
				Parser parser = Parser.createParser(content, Encoding.UTF8.type);
				for (NodeIterator i = parser.elements(); i.hasMoreNodes();){
					sb.append(HtmlUtil.replaceImgSrc2LazyLoad(i.nextNode(), sb).toHtml());
				}			
				return sb.toString();
			} catch (Exception e) {
				logger.error("Fail to processContent4Mobile, content=" + getPlainText(content, 128, false)
						+ ", e=" + e.getMessage(), e);
			}
		}
		return null;
	}
	
	/************
	 * 更改图片属性， 设置为lazy load
	 * @param node
	 * @param nodeStr
	 * @return
	 */
	private static Node replaceImgSrc2LazyLoad(Node node, StringBuilder nodeStr) 
	{
		String tagName;
		TagNode tagNode;
		if (node instanceof TagNode) 
		{
			tagNode = (TagNode) node;
			tagName = tagNode.getTagName();
			if ("img".equalsIgnoreCase(tagName)) {
				final String imgSrc = tagNode.getAttribute("src");				
				if(imgSrc != null){
					tagNode.setAttribute("src", "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==");
					tagNode.setAttribute("data-src", imgSrc); //赛会通
					tagNode.setAttribute("data-lazy", imgSrc); //别宅啦
				}
			}else{
				NodeList children = node.getChildren();
				if (children != null && children.size() > 0) {
					try {
						for (NodeIterator i = children.elements(); i.hasMoreNodes();){
							replaceImgSrc2LazyLoad(i.nextNode(),  nodeStr);
						}
					} catch (Exception e) {
						logger.error("Fail to replaceImgSrc2LazyLoad, node=" + node.toHtml() + ", ex=" +e.getMessage(), e);
					}
				}
			}
		}
		return node;
	}
	
	/************************
	 * 用新的图片路径替换富文本中的老路径， 这个主要是用于抓取图片之后替换老图片
	 * @param content
	 * @param srcMap
	 * @return
	 */
	public static String processContent4ByMigrateImageSrc2Saihuitong(final long sasId, final long userId, final String content) 
	{
		if (!StringUtils.isBlank(content)) {
			try {
				StringBuilder sb = new StringBuilder();
				Parser parser = Parser.createParser(content, Encoding.UTF8.type);
				for (NodeIterator i = parser.elements(); i.hasMoreNodes();){
					sb.append(HtmlUtil.replaceCompressImgSrcByMigrateImageSrc2Saihuitong(sasId, userId, i.nextNode(), sb).toHtml());
				}			
				return sb.toString();
			} catch (Exception e) {
				logger.error("Fail to processContent4ByMigrateImageSrc2Saihuitong, content=" + getPlainText(content, 128, false)
						+ ", e=" + e.getMessage(), e);
			}
		}
		return "";
	}
	
	//替换图片路径
	private static Node replaceCompressImgSrcByMigrateImageSrc2Saihuitong(final long sasId, final long userId, 
			Node node, StringBuilder nodeStr) 
	{
		final Pattern dataSrcPattern = Pattern.compile(IMGURL_REG_DATA_SRC);
		String tagName;
		TagNode tagNode;
		if (node instanceof TagNode) 
		{
			tagNode = (TagNode) node;
			tagName = tagNode.getTagName();
			if ("img".equalsIgnoreCase(tagName)) 
			{
				String imgSrc = tagNode.getAttribute("src");
				if(StringUtils.isNotBlank(imgSrc) && imgSrc.contains("data:image")){
					imgSrc = "";
				}					
				if(StringUtils.isBlank(imgSrc) || ThirdpartUtil.isNotSaihuitongImageURL(imgSrc))
				{
					String newURL = null;
					if(ValidatorUtil.URLValidate(imgSrc)){
						newURL = IOUtil.migrateOnePictureToQiniuByCaringWeixin(sasId, userId, imgSrc, 2);
					}
					if(StringUtils.isBlank(newURL))//尝试data-src, 解决微信问题
					{
						final Matcher dataSrcMatcher = dataSrcPattern.matcher(tagNode.toHtml());
						if(dataSrcMatcher.find())
						{
							imgSrc = dataSrcMatcher.group(1);
							if(ValidatorUtil.URLValidate(imgSrc))
							{
								if(ThirdpartUtil.isNotSaihuitongImageURL(imgSrc)){
									newURL = IOUtil.migrateOnePictureToQiniuByCaringWeixin(sasId, userId, imgSrc, 2);
								}else{
									newURL = imgSrc;
								}
							}
						}
					}
					if(StringUtils.isNotBlank(newURL))
					{
						tagNode.setAttribute("src", newURL);
						tagNode.removeAttribute("data-src");
						tagNode.removeAttribute("data-type"); //微信的一些属性
						tagNode.removeAttribute("data-w");
						tagNode.removeAttribute("data-ratio");
					}
				}
			}else{
				NodeList children = node.getChildren();
				if (children != null && children.size() > 0) {
					try {
						for (NodeIterator i = children.elements(); i.hasMoreNodes();){
							replaceCompressImgSrcByMigrateImageSrc2Saihuitong(sasId, userId, i.nextNode(), nodeStr);
						}
					} catch (Exception e) {
						logger.error("Fail to replaceCompressImgSrcByMigrateImageSrc2Saihuitong, node="
								+ node.toHtml() + ", ex=" +e.getMessage(), e);
					}
				}
			}
		}
		return node;
	}
	
	
	/************************
	 * 删除html文件中的空格符[换成&nbsp;]和换行符[换成<br/>]
	 * @param content
	 * @return
	 */
	public static final String repalceBlankAndNewLineChar2HTML(String content)
	{
		if(StringUtils.isBlank(content)){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");
		boolean isInTagElement = false;
		for (int i = 0; i < content.length(); i++) {
			final char c = content.charAt(i);
			if(c == '<'){
				isInTagElement = true;
			}else if(c == '>'){
				isInTagElement = false;
			}			
			if(c == '\n'){
				if(isInTagElement){
					sb.append(" ");
				}else{
					sb.append("<br/>");
				}
			}else if(c == '\r'){
				continue;
			}else if(c == ' ' || c == '\t'){
				if(isInTagElement){
					sb.append(" ");
				}else{
					sb.append("&nbsp;");
				}
			}else{
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/********************
	 * 对富文本中的图片和静态文本进行
	 * @param content
	 * @param imgSizePostFix
	 * @return
	 */
	public static String fetchPlainTextAndImages(final String content) 
	{
		if(StringUtils.isBlank(content)){
			return "";
		}
		final List<String> emotionImageUrls = new LinkedList<String>();
		final List<String> imageUrls = new LinkedList<String>();
		final StringBuilder plainText = new StringBuilder("");
		try {
			Parser parser = Parser.createParser(content, Encoding.UTF8.type);
			for (NodeIterator i = parser.elements(); i.hasMoreNodes();){
				fetchPlainTextAndImages(i.nextNode(), plainText, emotionImageUrls, imageUrls);
			}
			for(final String url : emotionImageUrls){
				plainText.append(url + " ");
			}	
			if(plainText.length() > 0){
				plainText.append("<br/>");
			}
			for(final String url : imageUrls){
				plainText.append(url + " ");
			}		
		} catch (Exception e) {
			logger.error("Fail to processContent4Mobile, content=" + getPlainText(content, 128, false)
					+ ", e=" + e.getMessage(), e);
		}
		return plainText.toString();
	}
	
	private static Node fetchPlainTextAndImages(Node node, final StringBuilder plainText, final List<String> emotionImageUrls,
			final List<String> imageUrls) 
	{
		String tagName;
		TagNode tagNode;
		if (node instanceof TagNode) 
		{
			tagNode = (TagNode) node;
			tagName = tagNode.getTagName();
			if ("img".equalsIgnoreCase(tagName)) {
				final String imgSrc = tagNode.getAttribute("src");
				if(StringUtils.isNotBlank(imgSrc)){
					if(imgSrc.contains("/emojis/")){
						emotionImageUrls.add("<img src='" + imgSrc + "'/>");
					}else if(imgSrc.contains(QiNiuSpace.SaihuitongImage.domain)){
						imageUrls.add("<img class='thumb' width='50px' height='50px' src='" + QiNiuUtil.generateQiNiuImgURL(imgSrc, QiNiuImageCompress.cw100h100.style) + "'/>");
					}else{
						imageUrls.add("<img width='50px' height='50px' src='" + imgSrc + "'/>");
					}
				}
			}else{
				NodeList children = node.getChildren();
				if (children != null && children.size() > 0) {
					try {
						for (NodeIterator i = children.elements(); i.hasMoreNodes();){
							fetchPlainTextAndImages(i.nextNode(), plainText, emotionImageUrls, imageUrls);
						}
					} catch (Exception e) {
						logger.error("Fail to fetchPlainTextAndImages, node=" + node.toHtml() + ", ex=" +e.getMessage(), e);
					}
				}
			}
		}else{
			plainText.append(node.getText());
		}
		return node;
	}
	
	/********************
	 * 对富文本中的图片路径添加压缩后缀
	 * @param content
	 * @param imgSizePostFix
	 * @return
	 */
	public static String processContent4Mobile(final String content, final String imgSizePostFix, final boolean usingImageLazyLoad) 
	{
		if (!StringUtils.isBlank(content)) {
			try {
				StringBuilder sb = new StringBuilder();
				Parser parser = Parser.createParser(content, Encoding.UTF8.type);
				for (NodeIterator i = parser.elements(); i.hasMoreNodes();){
					sb.append(replaceCompressImgSrc(i.nextNode(), imgSizePostFix, sb, usingImageLazyLoad).toHtml());
				}			
				return sb.toString();
			} catch (Exception e) {
				logger.error("Fail to processContent4Mobile, content=" + getPlainText(content, 128, false)
						+ ", e=" + e.getMessage(), e);
			}
		}
		return null;
	}
	
	private static Node replaceCompressImgSrc(Node node, final String imgSizePostFix, StringBuilder nodeStr, 
			final boolean usingImageLazyLoad) 
	{
		String tagName;
		TagNode tagNode;
		if (node instanceof TagNode) 
		{
			tagNode = (TagNode) node;
			tagName = tagNode.getTagName();
			if ("img".equalsIgnoreCase(tagName)) {
				final String imgSrc = tagNode.getAttribute("src");
				if(imgSrc != null && imgSrc.contains(QiNiuSpace.SaihuitongImage.domain)){
					if(usingImageLazyLoad){
						tagNode.setAttribute("src", "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==");
						final String imageUrl = QiNiuUtil.generateQiNiuImgURL(imgSrc, imgSizePostFix);
						tagNode.setAttribute("data-src", imageUrl);
						tagNode.setAttribute("data-lazy", imageUrl);
					}else{
						tagNode.setAttribute("src", QiNiuUtil.generateQiNiuImgURL(imgSrc, imgSizePostFix));
					}
					//tagNode.removeAttribute("width");
					//tagNode.removeAttribute("height");
				}
			}else{
				NodeList children = node.getChildren();
				if (children != null && children.size() > 0) {
					try {
						for (NodeIterator i = children.elements(); i.hasMoreNodes();){
							replaceCompressImgSrc(i.nextNode(), imgSizePostFix, nodeStr, usingImageLazyLoad);
						}
					} catch (Exception e) {
						logger.error("Fail to replaceCompressImgSrc, node="
								+ node.toHtml() + ", ex=" +e.getMessage(), e);
					}
				}
			}
		}
		return node;
	}
	
	/**********************
	 * 获取一个富文本的摘要内容，最多不超过length个字符
	 * @param content
	 * @param length
	 * @return
	 */
	public static String getBrief(String content, int length) {
		if (content == null) {
			return "";
		}
		final StringBuilder result = new StringBuilder();
		Parser parser = Parser.createParser(content, Encoding.UTF8.type);
		try {
			NodeIterator iter = parser.elements();
			while (iter.hasMoreNodes()) {
				Node node = iter.nextNode();
				if (node != null) 
				{
					if (node instanceof TagNode) 
					{
						final String tagName = ((TagNode)node).getTagName();
						if("style".equalsIgnoreCase(tagName)){
							continue;
						}
					}
					final String subString = node.toPlainTextString().replaceAll(WhiteSpaceReg, " ");
					if(length < 0){
						result.append(subString);
					}else{						
						final BinaryEntry<String, Integer> subStringResult = subStringWithEnglish2Letters(subString, length);
						if(subStringResult.key.length() > 0){
							result.append(subStringResult.key);
						}
						length = length - subStringResult.value;
						if(length <= 0){
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Fail getBrief(String, int), e=" + e.getMessage(), e);
		}
		return result.toString();
	}
	
	/**************
	 * 取子串， 但是考虑中英文, 英文算半个字符， 返回子串以及子串考虑英文之后的长度
	 * @param str
	 * @return
	 */
	public static final BinaryEntry<String, Integer> subStringWithEnglish2Letters(final String str, final int length)
	{
		if(str == null || length == 0){
			return new BinaryEntry<String, Integer>("", 0);
		}
		if(length < 0 || str.length() <= length){
			return new BinaryEntry<String, Integer>(str, str.length());
		}
		final StringBuilder result = new StringBuilder("");
		int resultLength = 0;
		boolean isPrevCharEnglish = false;
		for(char ch : str.toCharArray())
		{
			if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == ' '){
				if(isPrevCharEnglish){
					resultLength ++;
					isPrevCharEnglish = false;
				}else{
					isPrevCharEnglish = true;
				}
			}else{
				resultLength ++;
			}
			result.append(ch);
			if(resultLength >= length){
				break;
			}
		}
		return new BinaryEntry<String, Integer>(result.toString(), resultLength);
	}
	
	/**************
	 * 保留固定数量的图片， 其实主要用于列表缓存， 例如文章列表
	 * @param content
	 * @param contentLength
	 * @param imgCount
	 * @return
	 */
	public static String getBriefWithImages(String content, int contentLength, final int imgCount) {
		final String brief = HtmlUtil.getBrief(content, contentLength);
		final List<String> images = imgCount > 0 ? HtmlUtil.filterLargeSaihuitongImageSrcs(content, imgCount, true) : null;
		if(CollectionUtils.isEmpty(images)){
			return brief;
		}
		final StringBuilder imageHtmls = new StringBuilder(""); 
		for(final String str : images){
			imageHtmls.append("<img src='" + str + "'>");
		}
		return "<p>" + brief + imageHtmls.toString() + "</p>";
	}
	
	/********************
	 * 修复html文本
	 * @param content
	 * @return
	 */
	public static String repairHtml(String content) {
		String result = null;
		if (content != null) {
			NodeList nodeList;
			try {
				Parser parser = Parser.createParser(new String(content.getBytes(Encoding.UTF8.type), 
						Encoding.UTF8.type), 
						Encoding.UTF8.type);
				nodeList = parser.parse(null);
				return nodeList.toHtml();
			} catch (Exception e) {
				logger.error("Fail to repairHtml(String), ex=" + e.getMessage(), e);
			}
		}
		return result == null ? content : result;
	}
	
	/*********************
	 * 将富文本的图片进行替换， 主要是在文本框显示带有图片的富文本内容，将图片用类似”【图片】“这样的字符来显示
	 * @param content
	 * @param imgReplaceString
	 * @param maxLength
	 * @param appendEllipis
	 * @return
	 */
	public static final String getPlainTextByReplaceImgNode(String content, final String imgReplaceString, 
			final int maxLength, final boolean keepEmoptions, final boolean appendEllipis)
	{
		if (!StringUtils.isBlank(content)) {
			try {
				final StringBuilder sb = new StringBuilder("");
				Parser parser = Parser.createParser(content, Encoding.UTF8.type);
				for (NodeIterator i = parser.elements(); i.hasMoreNodes();){
					HtmlUtil.createPlainTextByReplaceImgNode(i.nextNode(), imgReplaceString, maxLength, sb, keepEmoptions);
				}			
				if(maxLength > 0 && sb.length() > maxLength){
					return appendEllipis ? sb.substring(0, maxLength)+"..." : sb.substring(0, maxLength);
				}else{
					return sb.toString();
				}
			} catch (Exception e) {
				logger.error("Fail to getPlainTextByReplaceImgNode, content=" + getPlainText(content, 128, false)
						+ ", e=" + e.getMessage(), e);
			}
		}
		return null;
	}
	
	/**************************
	 * 将图片替换成一段文本
	 * @param node
	 * @param imgReplaceString
	 * @param maxLength
	 * @param nodeStr
	 */
	private static final void createPlainTextByReplaceImgNode(Node node, final String imgReplaceString, 
			final int maxLength, StringBuilder nodeStr, final boolean keepEmoptions) 
	{
		if(nodeStr.length() >= maxLength){
			return;
		}
		String tagName;
		TagNode tagNode;
		if (node instanceof TagNode) 
		{
			tagNode = (TagNode) node;
			tagName = tagNode.getTagName();
			if ("img".equalsIgnoreCase(tagName)) {
				if(keepEmoptions){
					final String imgSrc = tagNode.getAttribute("src");
					if(imgSrc.contains("/emojis/")){
						nodeStr.append("<img src='" + imgSrc + "'>");
					}else{
						nodeStr.append(imgReplaceString);
					}
				}else{
					nodeStr.append(imgReplaceString);
				}
			}else{
				NodeList children = node.getChildren();
				if (children != null && children.size() > 0) {
					try {
						for (NodeIterator i = children.elements(); i.hasMoreNodes();){
							createPlainTextByReplaceImgNode(i.nextNode(), imgReplaceString, maxLength, nodeStr, keepEmoptions);
						}
					} catch (Exception e) {
						logger.error("Fail to createPlainTextByReplaceImgNode, node="
								+ node.toHtml() + ", ex=" +e.getMessage(), e);
					}
				}else{
					nodeStr.append(node.toPlainTextString().replaceAll("\\s+", " ").replaceAll("((\\r\\n)\\s?)+", "\r\n"));
				}
			}
		}else{
			nodeStr.append(node.toPlainTextString().replaceAll("\\s+", " ").replaceAll("((\\r\\n)\\s?)+", "\r\n").replaceAll("&nbsp;", ""));
		}
	}
	
	/******************
	 * 获取纯文本
	 * @param content
	 * @param maxLength
	 * @param appendEllipis
	 * @return
	 */
	public static String getPlainText(String content, int maxLength, final boolean appendEllipis) 
	{
		if(maxLength == 0){
			return "";
		}
		String result = null;
		if (content != null) 
		{
			NodeList nodeList;
			try {
				Parser parser = Parser.createParser(new String(content.replaceAll("(<style>[^<]{1,500}</style>)|(<STYLE>[^<]{1,500}</STYLE>)", "").getBytes(Encoding.UTF8.type), Encoding.UTF8.type), 
						Encoding.UTF8.type);
				nodeList = parser.parse(null);
				result = nodeList.asString();
			} catch (Throwable e) {
				logger.error("getPlainText(String)", e);
			}
			if (result != null){
				result = result.replaceAll(WhiteSpaceReg, " ");
			}
		}
		if (result == null ){
			return "";
		}else{
			final BinaryEntry<String, Integer> subStringResult = subStringWithEnglish2Letters(result, maxLength);
			return appendEllipis ? subStringResult.key +"..."
					: subStringResult.key;
		}
	}


	/**
	 * 修复当前html节点
	 * @param node
	 * @param sb
	 */
	private static void processMyNodes(Node node, StringBuilder sb) {
		String tagName;
		TagNode tagNode;
		if (node instanceof TagNode) {
			tagNode = (TagNode) node;
			tagName = tagNode.getTagName();
			if ("br".equalsIgnoreCase(tagName)) {
				sb.append("<br/>");
			} else if ("p".equalsIgnoreCase(tagName)) {
				sb.append("<p>");
				sb.append(tagNode.toPlainTextString().replaceAll(WhiteSpaceReg, ""));
				sb.append("</p>");
			} else {
				NodeList children = tagNode.getChildren();
				if (children != null && children.size() > 0) {
					try {
						for (NodeIterator i = children.elements(); i.hasMoreNodes();)
							processMyNodes(i.nextNode(), sb);
					} catch (Exception e) {
						logger.error("Fail to processMyNodes, ex="+e.getMessage(), e);
					}
				}
			}
		} else {
			sb.append(node.toPlainTextString());
		}
	}
	
	/***********************
	 * 删除富文本中未指定保留的tag
	 * @param content
	 * @param keepTagNames
	 * @return
	 */
	public static final String processActivityHTMLByRemoveNodes(String content, String[] keepTagNames)
	{
		if(StringUtils.isBlank(content)){
			return "";
		}
		if (content != null) {
			try {
				final StringBuilder sb = new StringBuilder();
				final Set<String> keepTagNameSet = new HashSet<String>();
				if(ArrayUtils.isNotEmpty(keepTagNames)){
					for(String tageName: keepTagNames){
						keepTagNameSet.add(tageName.toLowerCase());
					}					
				}
				Parser parser = Parser.createParser(content, Encoding.UTF8.type);
				for (NodeIterator i = parser.elements(); i.hasMoreNodes();){
					processTagsByRemoveNodes(i.nextNode(), sb, keepTagNameSet, true);
				}
				return sb.toString();
			} catch (Exception e) {
				logger.error("getPlainText(String)", e);
			}
		}
		return null;
	}
	
	/***********************
	 * 
	 * @param content
	 * @param keepTagNames
	 * @param needKeepTagAttribute
	 * @return
	 */
	public static final String removeBBSHTMLByNodes(String content, String[] keepTagNames, 
			final boolean needKeepTagAttribute)
	{
		if(StringUtils.isBlank(content)){
			return "";
		}
		content = content.replaceAll("\r\n", "<br/>");
		if (content != null) {
			try {
				final StringBuilder sb = new StringBuilder();
				final Set<String> keepTagNameSet = new HashSet<String>();
				if(ArrayUtils.isNotEmpty(keepTagNames)){
					for(String tageName: keepTagNames){
						keepTagNameSet.add(tageName.toLowerCase());
					}					
				}
				Parser parser = Parser.createParser(content, Encoding.UTF8.type);
				for (NodeIterator i = parser.elements(); i.hasMoreNodes();){
					processTagsByRemoveNodes(i.nextNode(), sb, keepTagNameSet, needKeepTagAttribute);
				}
				return sb.toString();
			} catch (Exception e) {
				logger.error("getPlainText(String)", e);
			}
		}
		return null;
	}
	
	private static void processTagsByRemoveNodes(Node node, StringBuilder sb, final Set<String> keepTagNameSet, 
			final boolean needKeepTagAttribute) 
	{
		if (node instanceof TagNode) 
		{
			final TagNode tagNode = (TagNode) node;
			final String tagName = tagNode.getTagName().toLowerCase();
			final NodeList children = tagNode.getChildren();
			if (children != null && children.size() > 0)
			{
				if(keepTagNameSet.contains(tagName)){
					sb.append(HtmlUtil.generateTagStartString(tagNode, needKeepTagAttribute, true));
				}
				try {
					for (NodeIterator i = children.elements(); i.hasMoreNodes();){
						processTagsByRemoveNodes(i.nextNode(), sb, keepTagNameSet, needKeepTagAttribute);
					}
				} catch (ParserException e) {
					logger.error("Fail to processTagsByRemoveNodes, ex="+e.getMessage(), e);
				}
				if(keepTagNameSet.contains(tagName)){
					sb.append("</" + tagName + ">");
				}				
			}else{
				if(keepTagNameSet.contains(tagName)){
					sb.append(HtmlUtil.generateTagStartString(tagNode, needKeepTagAttribute, false));
				}else{
					sb.append(node.toPlainTextString().replaceAll("\\n", "<br/>").replaceAll("\\s", "&nbsp;"));
				}
			}
		} else {
			sb.append(node.toPlainTextString().replaceAll("\\n", "<br/>").replaceAll("\\s", "&nbsp;"));
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final String generateTagStartString(final TagNode tagNode, final boolean needKeepTagAttribute, 
			final boolean hasChildrenNode){
		final StringBuilder sb = new StringBuilder("");
		final String tagName = tagNode.getTagName().toLowerCase();
		sb.append("<" + tagName);
		if(needKeepTagAttribute){
			final Attribute[] attributes = (Attribute[]) tagNode.getAttributesEx().toArray(new Attribute[0]);
			for (Attribute attr : attributes){
				if(attr.isEmpty() || StringUtils.isBlank(attr.getValue()) || tagName.equals(attr.getName())){
					continue;
				}
				sb.append(" " + attr.getName() + "='" + attr.getValue() + "'");
			}
		}
		if(hasChildrenNode){
			sb.append(">");
		}else{
			sb.append("/>");
		}
		return sb.toString();
	}
	
	/********************
	 * 获取纯文本
	 * @param content
	 * @return
	 */
	public static String getPlainTextWithTag(String content) {
		if (content != null) {
			try {
				StringBuilder sb = new StringBuilder(1024);
				Parser parser = Parser.createParser(content, Encoding.UTF8.type);
				for (NodeIterator i = parser.elements(); i.hasMoreNodes();)
					processMyNodes(i.nextNode(), sb);
				return sb.toString();
			} catch (Exception e) {
				logger.error("getPlainText(String)", e);
			}
		}
		return null;
	}
	
	public static final String escapeStringToHtmlNullIfNull(String str) {
		if (str == null) {
			return null;
		}
		return escapeStringToHtml(str);
	}
	
	
	public static final String escapeStringToHtml(final String str)
	{
		if (str == null || str.length() < 1) {
			return "";
		}
		final StringBuilder html = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			switch (c) {
			case '<':
				html.append("&lt;");
				break;
			case '>':
				html.append("&gt;");
				break;
			case '&':
				html.append("&amp;");
				break;
			case '"':
				html.append("&quot;");
				break;
			case '\u00E0':
				html.append("&agrave;");
				break;
			case '\u00C0':
				html.append("&Agrave;");
				break;
			case '\u00E2':
				html.append("&acirc;");
				break;
			case '\u00C2':
				html.append("&Acirc;");
				break;
			case '\u00E4':
				html.append("&auml;");
				break;
			case '\u00C4':
				html.append("&Auml;");
				break;
			case '\u00E5':
				html.append("&aring;");
				break;
			case '\u00C5':
				html.append("&Aring;");
				break;
			case '\u00E6':
				html.append("&aelig;");
				break;
			case '\u00C6':
				html.append("&AElig;");
				break;
			case '\u00E7':
				html.append("&ccedil;");
				break;
			case '\u00C7':
				html.append("&Ccedil;");
				break;
			case '\u00E9':
				html.append("&eacute;");
				break;
			case '\u00C9':
				html.append("&Eacute;");
				break;
			case '\u00E8':
				html.append("&egrave;");
				break;
			case '\u00C8':
				html.append("&Egrave;");
				break;
			case '\u00EA':
				html.append("&ecirc;");
				break;
			case '\u00CA':
				html.append("&Ecirc;");
				break;
			case '\u00EB':
				html.append("&euml;");
				break;
			case '\u00CB':
				html.append("&Euml;");
				break;
			case '\u00EF':
				html.append("&iuml;");
				break;
			case '\u00CF':
				html.append("&Iuml;");
				break;
			case '\u00F4':
				html.append("&ocirc;");
				break;
			case '\u00D4':
				html.append("&Ocirc;");
				break;
			case '\u00F6':
				html.append("&ouml;");
				break;
			case '\u00D6':
				html.append("&Ouml;");
				break;
			case '\u00F8':
				html.append("&oslash;");
				break;
			case '\u00D8':
				html.append("&Oslash;");
				break;
			case '\u00DF':
				html.append("&szlig;");
				break;
			case '\u00F9':
				html.append("&ugrave;");
				break;
			case '\u00D9':
				html.append("&Ugrave;");
				break;
			case '\u00FB':
				html.append("&ucirc;");
				break;
			case '\u00DB':
				html.append("&Ucirc;");
				break;
			case '\u00FC':
				html.append("&uuml;");
				break;
			case '\u00DC':
				html.append("&Uuml;");
				break;
			case '\u00AE':
				html.append("&reg;");
				break;
			case '\u00A9':
				html.append("&copy;");
				break;
			case '\u20AC':
				html.append("&euro;");
				break;
			default:
				html.append(c);
				break;
			}
		}
		return html.toString();
	}
	
	
	public static String unescape(String txt, final boolean needExcludeWhiteSpace) {
		if (StringUtils.isBlank(txt)) {
			return txt;
		}
		final char[] array = txt.toCharArray();
		final StringBuilder builder = new StringBuilder(array.length);
		for (int i = 0; i < array.length; i++) {
			final char c = array[i];
			if (c == '&') {
				final int idx = ArrayUtils.indexOf(array, ';', i);
				if (idx > i) {
					final char[] tempCs = ArrayUtils.subarray(array, i, idx + 1);
					final String key = new String(tempCs);
					final Character character = unescapeMap.get(key);
					if (character != null && (!needExcludeWhiteSpace || character != ' ')) {
						builder.append(character);
					} else {
						builder.append(key);
					}
					i = idx;
				} else {
					builder.append(ArrayUtils.subarray(array, i, array.length));
					break;
				}
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}
	
	/***********
	 * 删除空的图片节点
	 * @param content
	 * @return
	 */
	public static String filterEmptyImageTag(String content) {
		String result = null;
		if (content != null) {
			NodeList nodeList;
			try {
				Parser parser = Parser.createParser(new String(content.getBytes(Encoding.UTF8.type),
						Encoding.UTF8.type), 
						Encoding.UTF8.type);
				nodeList = parser.parse(null);
				removeEmptyImageTag(nodeList);
				result = nodeList.toHtml();
			} catch (Exception e) {
				logger.error("filterEmptyImageTag(String), e="+e.getMessage(), e);
			}
		}
		return result;
	}
	
	/*********************
	 * 删除图片节点
	 * @param nodeList
	 */
	public static void removeEmptyImageTag(NodeList nodeList) {
		if (nodeList == null || nodeList.size() == 0)
			return;
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.elementAt(i);
			if (node instanceof ImageTag) {
				ImageTag tag = (ImageTag) node;
				if (tag != null && StringUtils.isEmpty(tag.getAttribute("src"))) {
					nodeList.remove(i);
				}
			} else {
				removeEmptyImageTag(node.getChildren());
			}
		}
	}
	
	/***
	 * 获取ImageSrc地址
	 * 
	 * @param content
	 * @return
	 */
	public static final List<String> filterImageSrcsByRegExpression(String content) {
		if(StringUtils.isBlank(content)){
			return new ArrayList<String>(0);
		}
		final List<String> listImgSrcs = new LinkedList<String>();
		final Set<String> allImageSrcSet = new HashSet<String>();
		final Pattern p = Pattern.compile(IMGURL_REG);
		final Matcher m = p.matcher(content);
		while (m.find()) {
			String src = m.group(3);
			if(StringUtils.isBlank(src) || src.startsWith("/") || src.contains("data:")){ //如果src非本域则优先取data-src等属性
				final Pattern dataSrcPattern = Pattern.compile(IMGURL_REG_DATA_SRC);
				final Matcher dataSrcMatcher = dataSrcPattern.matcher(m.group(0));
				if(dataSrcMatcher.find()){//优先取data-src, 解决微信问题
					final String tmpSrc = dataSrcMatcher.group(1);
					if(HtmlUtil.isValidImageURL(tmpSrc)){
						src = tmpSrc;
					}
				}
			}
			if(StringUtils.isNotBlank(src) && !allImageSrcSet.contains(src)){
				listImgSrcs.add(src);
				allImageSrcSet.add(src);
			}
		}
		return listImgSrcs;
	}
	
	public static final List<String> filterSimpleImageSrcsByRegExpression(String content) {
		if(StringUtils.isBlank(content)){
			return new ArrayList<String>(0);
		}
		final List<String> listImgSrcs = new LinkedList<String>();
		Pattern p = Pattern.compile(IMGURL_REG);
		Matcher m = p.matcher(content);
		while (m.find()) {
			final String src = m.group(3);
			if(src != null){
				listImgSrcs.add(src.trim());
			}
		}
		return listImgSrcs;
	}
	
	public static final String filterFirstSimpleImageSrcsByRegExpression(String content) {
		if(StringUtils.isBlank(content)){
			return null;
		}
		Pattern p = Pattern.compile(IMGURL_REG);
		Matcher m = p.matcher(content);
		while (m.find()) {
			final String src = m.group(3);
			if(src != null){
				return src.trim();
			}
		}
		return null;
	}
	
	/*****************
	 * 过滤所有有效的大图【除去表情】
	 * @param content
	 * @param count
	 * @return
	 */
	public static final List<String> filterValidLargeImageSrcs(String content, final int count,
			final boolean needRemoveCompressStyle, final boolean onlySaihuitongImages) 
	{
		final List<String> allImages = HtmlUtil.filterImageSrcsByRegExpression(content);
		if(allImages.size() < 1){
			return new ArrayList<String>(0);		
		}
		//先找七牛的图片
		final List<String> list = new LinkedList<String>();
		for(final String url : allImages)
		{
			if(QiNiuSpace.isQiNiuImageURL(url))
			{
				if(ImageSizeType.Small.isMySize(url)){
					continue;
				}
				if(needRemoveCompressStyle){
					list.add(QiNiuUtil.removeCompressStyle(url));
				}else{
					list.add(url);
				}
			}else if(!onlySaihuitongImages && HtmlUtil.isValidImageURL(url)){
				list.add(url);
			}
			if(count > 0 && list.size() >= count){
				break;
			}
		}
		return list;
	}

	public static final List<String> filterLargeSaihuitongImageSrcs(String content, final int count, final boolean needRemoveCompressStyle) {
		final List<String> allImages = HtmlUtil.filterImageSrcsByRegExpression(content);
		if(allImages.size() < 1){
			return new ArrayList<String>(0);		
		}
		//先找七牛的图片
		final List<String> list = new LinkedList<String>();
		for(final String url : allImages){
			if(QiNiuSpace.isQiNiuImageURL(url)){
				if(ImageSizeType.Small.isMySize(url)){
					continue;
				}
				if(needRemoveCompressStyle){
					list.add(QiNiuUtil.removeCompressStyle(url));
				}else{
					list.add(url);
				}
			}
			if(count > 0 && list.size() >= count){
				break;
			}
		}
		return list;
	}
	
	public static final List<String> filterAllSaihuitongImageSrcs(String content, final int count, final boolean needRemoveCompressStyle) {
		final List<String> allImages = HtmlUtil.filterImageSrcsByRegExpression(content);
		if(allImages.size() < 1){
			return new ArrayList<String>(0);		
		}
		//先找七牛的图片
		final List<String> list = new LinkedList<String>();
		for(final String url : allImages){
			if(QiNiuSpace.isQiNiuImageURL(url)){
				if(needRemoveCompressStyle){
					list.add(QiNiuUtil.removeCompressStyle(url));
				}else{
					list.add(url);
				}
			}
			if(count > 0 && list.size() >= count){
				break;
			}
		}
		return list;
	}
		
	/***************
	 * 获取第一张图片
	 * @param content
	 * @return
	 */
	public static final String filterFirstImageSrc(String content, final boolean needRemoveCompressStyle)
	{
		final List<String> list = HtmlUtil.filterValidLargeImageSrcs(content, 1, needRemoveCompressStyle, true);
		return CollectionUtils.isEmpty(list) ? "" : list.get(0);
	}
	
	public static final String filterFirstSaihuitongImageSrc(String content, final boolean needRemoveCompressStyle)
	{
		final List<String> list = HtmlUtil.filterLargeSaihuitongImageSrcs(content, 1, needRemoveCompressStyle);
		return CollectionUtils.isEmpty(list) ? "" : list.get(0);
	}
	
	/************
	 * 是否是有效的图片路径
	 * @param url
	 * @return
	 */
	public static final boolean isValidImageURL(String url){
		if(StringUtils.isBlank(url)){
			return false;
		}
		url = url.toLowerCase();
		//忽略表情， 不用抓取， 不算图片
		if(HtmlUtil.isEmotionOrLocalFileURL(url)){
			return false;
		}	
		if(validImageURLPatternSearchAlgorithm == null){
			synchronized(HtmlUtil.class)
			{
				if(validImageURLPatternSearchAlgorithm == null)
				{
					validImageURLPatternSearchAlgorithm = new SearchAlgorithm(false);
					validImageURLPatternSearchAlgorithm.addKeyword("qq.com");
					validImageURLPatternSearchAlgorithm.addKeyword("http:");
					validImageURLPatternSearchAlgorithm.addKeyword("https:");
					validImageURLPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}	
		return CollectionUtils.isNotEmpty(validImageURLPatternSearchAlgorithm.SearchKeyWords(url))
				|| ((url.startsWith("data:") && url.length()<8192 ));
	}
	
	/*****************
	 * 是否是表情或者非法的本地图片路径
	 * @param urlInLowCase
	 * @return
	 */
	public static final boolean isEmotionOrLocalFileURL(final String urlInLowCase)
	{
		if(inValidImageURLPatternSearchAlgorithm == null){
			synchronized(HtmlUtil.class)
			{
				if(inValidImageURLPatternSearchAlgorithm == null)
				{
					inValidImageURLPatternSearchAlgorithm = new SearchAlgorithm(false);
					inValidImageURLPatternSearchAlgorithm.addKeyword(EmotionUtil.EmotionURLPRefix);
					inValidImageURLPatternSearchAlgorithm.addKeyword("file:");
					inValidImageURLPatternSearchAlgorithm.buildAlgorithm();
				}
			}
		}		
		//忽略表情， 不用抓取， 不算图片
		if(CollectionUtils.isNotEmpty(inValidImageURLPatternSearchAlgorithm.SearchKeyWords(urlInLowCase))){
			return true;
		}
		return false;
	}
	
	/****************
	 * decoding 网络参数
	 * @param v
	 * @param code
	 * @param defaultValue
	 * @return
	 */
	public static final String decodeParam(final String v, final Encoding code, final String defaultValue){
		if(v == null){
			return defaultValue;
		}
		try {
			return URLDecoder.decode(v, code.type);
		} catch (UnsupportedEncodingException e) {
			logger.error("Fail to decode " + v , e);
			return defaultValue;
		}
	}
	
	/*************
	 * 对中文进行encoding
	 * @param v
	 * @param code
	 * @return
	 */
	public static final String encodeParam(final String v, final Encoding code){
		if(v == null){
			return v;
		}
		return RequestUtil.encodeURLParam(v, v);
	}
	
	/************
	 * 对中文参数进行转码
	 * @param p
	 * @param fromEncoding
	 * @param toEncoding
	 * @return
	 */
	public static final String convertParamEncoding(final String p, final Encoding fromEncoding, final Encoding toEncoding)
	{
		if(StringUtils.isBlank(p)){
			return "";
		}
		try{
			return new String(p.getBytes(fromEncoding.type),toEncoding.type);
		}catch(Exception ex){
			logger.error("Fail to convertParamEncoding, p=" + p, ex);
			return "";
		}
	}

	/*******************
	 * 判断是否是乱码， 再进行编码转换
	 * @param p
	 * @param fromEncoding
	 * @param toEncoding
	 * @return
	 */
	public static final String convertParamEncodingWithCheckEncoding(final String p, final Encoding fromEncoding, final Encoding toEncoding){
		if(StringUtils.isBlank(p)){
			return p;
		}
		return HtmlUtil.isStringErrorEncoding(p) ? HtmlUtil.convertParamEncoding(p, fromEncoding, toEncoding) : p;
	}
	
	/*******************
	 * 是否是乱码字符
	 * @param strName
	 * @return
	 */
	public static boolean isStringErrorEncoding(final String strName)
	{
		final Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		final Matcher m = p.matcher(strName);
		final String after = m.replaceAll("");
		final String temp = after.replaceAll("\\p{P}", ""); //删除符号
	    char[] ch = temp.trim().toCharArray();
	    float chLength = ch.length;
	    float count = 0;
	    for (int i = 0; i < ch.length; i++) {
	        if (!isChineseOrLetterDigit(ch[i])) {
	          count = count + 1;
	        }
	    }
	    float result = count / chLength;
	    if (result > 0.1) {
	        return true;
	    } else {
	        return false;
	    }
	}
	 

	/***************
	 * 是否是中文， 数字 或者字符
	 * @param c
	 * @return
	 */
	public static boolean isChineseOrLetterDigit(final char c) {
		return Character.isLetterOrDigit(c) || HtmlUtil.isChineseLetter(c);
	}
	  
	 /*********
	  * 根据Unicode编码完美的判断中文汉字和符号
	  * @param c
	  * @return
	  */
	 public static final boolean isChineseLetter(char c) {
	     if (c >= '\u4e00' && c <= '\u9fa5') {
	         return true;
	     }
	     return false;
	 }
	
	 //是否是数字
	 public static final boolean isDigit(char c){
		 return c >= '0' && c <= '9';
	 }
	 
	 //是否是字母
	 public static final boolean isEnglishLetter(char c){
		 return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	 }
	 
	/**
	 * 將數組轉換成String
	 * @param convertArrayToString
	 * @return
	 */
	public static final String convertArrayToString(final String[] array, final boolean needXSSFilter, final DividerChar dividerChar){
		if(array == null || array.length < 1){
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer();
		for(int index = 0; index < array.length; index++){
			if(StringUtils.isBlank(array[index])){
				continue;
			}
			if(needXSSFilter){
				stringBuffer.append(XSSUtil.filter(array[index], false));
			}else{
				stringBuffer.append(array[index]);
			}
			if(index != (array.length - 1)){
				stringBuffer.append(dividerChar.chars);
			}
		}
		return stringBuffer.toString();
	}
	
	/**
	 * 將String轉換成數組
	 * @param string
	 * @param needXSSFilter
	 * @param dividerChar
	 * @return
	 */
	public static final String[] convertStringToArray(final String string, final boolean needXSSFilter, final DividerChar dividerChar,
			final QiNiuImageCompress qiNiuImageCompress){		
		if(StringUtils.isBlank(string)){
			return new String[0];
		}
		final String[] array = string.split(dividerChar.chars);
		for(int index = 0; index < array.length; index ++){
			if(needXSSFilter){
				array[index] = XSSUtil.filter(array[index], false);
			}
			if(qiNiuImageCompress != null){
				array[index] = QiNiuUtil.generateQiNiuImgURL(array[index], qiNiuImageCompress.style);
			}
		}
		return array;
	}

	/*****************
	 * 如果为空或者相等， 则返回默认值
	 * @param sourceString
	 * @param equal
	 * @param defaultString
	 * @return
	 */
	public  static final String defaultIfEqual(final String sourceString, final String equal, final String defaultString)
	{
		if(sourceString == null || equal == null || sourceString.equals(equal)){
			return defaultString;
		}
		return sourceString;
	}
	
	public  static final String defaultIfNull(final Object sourceString, final String defaultString)
	{
		if(sourceString == null){
			return defaultString;
		}
		return (String)sourceString;
	}
	
	/*****************
	 * 优化 url
	 * @param url
	 * @return
	 */
	public static final String optimizeURL(String url){
		if(url != null)
		{
			url = url.trim();
			if(url.length() < 5){
				return "";
			}else if(!RequestUtil.isStartWithHttpOrHttps(url)){
				return "http://" + url;
			}
			return url;
		}else{
			return "";
		}
	}
	
	/*******************
	 * 删除utf8mb4的手机表情， 暂不支持显示
	 * @param content
	 * @return
	 */
	public static Map<String, String> allIOSEmotionsMap;
	static{
		allIOSEmotionsMap = new HashMap<String, String>();
		allIOSEmotionsMap.put("\uD83D\uDE3F", "[冷汗]"); allIOSEmotionsMap.put("\uD83D\uDE3B", "[色]");   allIOSEmotionsMap.put("\uD83D\uDE40", "[惊讶]"); 
		allIOSEmotionsMap.put("\uD83D\uDE3D", "[亲亲]"); allIOSEmotionsMap.put("\uD83D\uDE3E", "[傲慢]"); 
		allIOSEmotionsMap.put("\uD83D\uDE3A", "[微笑]"); allIOSEmotionsMap.put("\uD83D\uDE39", "[憨笑]"); allIOSEmotionsMap.put("\uD83D\uDE3C", "[得意]"); 
		allIOSEmotionsMap.put("\uD83D\uDE13", "[冷汗]"); allIOSEmotionsMap.put("\uD83D\uDE01", "[呲牙]"); allIOSEmotionsMap.put("\uD83D\uDE38", "[憨笑]"); 
		allIOSEmotionsMap.put("\uD83D\uDE2E", "[惊讶]"); allIOSEmotionsMap.put("\uD83D\uDE1E", "[快哭了]"); allIOSEmotionsMap.put("\uD83D\uDE19", "[亲亲]"); 
		allIOSEmotionsMap.put("\uD83D\uDE24", "[发怒]"); allIOSEmotionsMap.put("\uD83D\uDE27", "[惊讶]"); allIOSEmotionsMap.put("\uD83D\uDE05", "[冷汗]"); 
		allIOSEmotionsMap.put("\uD83D\uDE25", "[冷汗]"); allIOSEmotionsMap.put("\uD83D\uDE1B", "[调皮]"); allIOSEmotionsMap.put("\uD83DDE16\uDE2B", "[折磨]");
		allIOSEmotionsMap.put("\uD83D\uDE2B", "[折磨]"); allIOSEmotionsMap.put("\u263A", "[微笑]");  allIOSEmotionsMap.put("\uD83D\uDE00", "[憨笑]"); 
		allIOSEmotionsMap.put("\uD83D\uDE18", "[示爱]"); allIOSEmotionsMap.put("\uD83D\uDE36", "[发呆]"); allIOSEmotionsMap.put("\uD83D\uDE2A", "[困]");
		allIOSEmotionsMap.put("\uD83D\uDE2F", "[惊讶]"); allIOSEmotionsMap.put("\uD83D\uDE26", "[惊讶]"); allIOSEmotionsMap.put("\uD83D\uDE21", "[发怒]"); 
		allIOSEmotionsMap.put("\uD83D\uDE02", "[憨笑]"); allIOSEmotionsMap.put("\uD83D\uDE1A", "[亲亲]"); allIOSEmotionsMap.put("\uD83D\uDE17", "[亲亲]"); 
		allIOSEmotionsMap.put("\uD83D\uDE0C", "[微笑]"); allIOSEmotionsMap.put("\uD83D\uDE04", "[憨笑]"); allIOSEmotionsMap.put("\uD83D\uDE37", "[微笑]"); 
		allIOSEmotionsMap.put("\uD83D\uDE0A", "[微笑]"); allIOSEmotionsMap.put("\uD83D\uDE12", "[撇嘴]"); allIOSEmotionsMap.put("\uD83D\uDE0D", "[色]"); 
		allIOSEmotionsMap.put("\uD83D\uDE0F", "[发呆]"); allIOSEmotionsMap.put("\uD83D\uDE0E", "[得意]"); allIOSEmotionsMap.put("\uD83D\uDE2D", "[流泪]");
		allIOSEmotionsMap.put("\uD83D\uDE20", "[发怒]"); allIOSEmotionsMap.put("\uD83D\uDE1C", "[调皮]");
		allIOSEmotionsMap.put("\uD83D\uDE1D", "[调皮]"); allIOSEmotionsMap.put("\uD83D\uDE1F", "[难过]");
		allIOSEmotionsMap.put("\uD83D\uDE03", "[呲牙]"); allIOSEmotionsMap.put("\uD83D\uDE2C", "[呲牙]"); allIOSEmotionsMap.put("\uD83D\uDE28", "[惊讶]"); 
		allIOSEmotionsMap.put("\uD83D\uDE29", "[难过]"); allIOSEmotionsMap.put("\uD83D\uDE23", "[难过]"); 
		allIOSEmotionsMap.put("\uD83D\uDE30", "[冷汗]"); allIOSEmotionsMap.put("\uD83D\uDE10", "[可爱]");
		allIOSEmotionsMap.put("\uD83D\uDE11", "[白眼]"); allIOSEmotionsMap.put("\uD83D\uDE15", "[傲慢]"); allIOSEmotionsMap.put("\uD83D\uDE34", "[困]");
		allIOSEmotionsMap.put("\uD83D\uDE06", "[憨笑]"); allIOSEmotionsMap.put("\uD83D\uDE07", "[晕]"); 
		allIOSEmotionsMap.put("\uD83D\uDE32", "[折磨]"); allIOSEmotionsMap.put("\uD83D\uDE14", "[衰]"); allIOSEmotionsMap.put("\uD83D\uDE31", "[骷髅]"); 
		allIOSEmotionsMap.put("\uD83D\uDE35", "[糗大了]"); allIOSEmotionsMap.put("\uD83D\uDE22", "[快哭了]"); allIOSEmotionsMap.put("\uD83D\uDE09", "[亲亲]"); 
		allIOSEmotionsMap.put("\uD83D\uDE33", "[可怜]"); 	allIOSEmotionsMap.put("\uD83D\uDE0B", "[示爱]"); 
		allIOSEmotionsMap.put("\u2600", "[:太阳]");		allIOSEmotionsMap.put("\2614", "[:下雨]");		allIOSEmotionsMap.put("\u2601", "[:云朵]");	
		allIOSEmotionsMap.put("\u26C4", "[:雪人]");		allIOSEmotionsMap.put("\uD83C\uDF19", "[:月亮]");		allIOSEmotionsMap.put("\u26A1", "[:闪电]");
		allIOSEmotionsMap.put("\uD83C\uDF00", "[:刮风]");		allIOSEmotionsMap.put("\uD83C\uDF0A", "[:大浪]" );
		allIOSEmotionsMap.put("\uD83D\uDC31", "[:狐狸]");		allIOSEmotionsMap.put("\uD83D\uDC36", "[:狗狗]" );		allIOSEmotionsMap.put("\uD83D\uDC2D", "[:老鼠]" );
		allIOSEmotionsMap.put("\uD83D\uDC39", "[:米老鼠]");	allIOSEmotionsMap.put("\uD83D\uDC30", "[:兔子]" );		allIOSEmotionsMap.put("\uD83D\uDC3A", "[:狼]" );
		allIOSEmotionsMap.put("\uD83D\uDC38", "[:青蛙]");		allIOSEmotionsMap.put("\uD83D\uDC2F", "[:老虎]" );		allIOSEmotionsMap.put("\uD83D\uDC28", "[:小熊]" );
		allIOSEmotionsMap.put("\uD83D\uDC3B", "[:灰熊]");		allIOSEmotionsMap.put("\uD83D\uDC37", "[:小猪]" );		allIOSEmotionsMap.put("\uD83D\uDC2E", "[:小羊]" );
		allIOSEmotionsMap.put("\uD83D\uDC17", "[:野猪]");		allIOSEmotionsMap.put("\uD83D\uDC35", "[:猴子]" );		allIOSEmotionsMap.put("\uD83D\uDC12", "[:小猴]" );
		allIOSEmotionsMap.put("\uD83D\uDC34", "[:小马]");		allIOSEmotionsMap.put("\uD83D\uDC0E", "[:奔跑]" );		allIOSEmotionsMap.put("\uD83D\uDC2B", "[:骆驼]" );
		allIOSEmotionsMap.put("\uD83D\uDC11", "[:小羊]");		allIOSEmotionsMap.put("\uD83D\uDC18", "[:大象]" );		allIOSEmotionsMap.put("\uD83D\uDC0D", "[:小蛇]" );
		allIOSEmotionsMap.put("\uD83D\uDC26", "[:小鸟]");		allIOSEmotionsMap.put("\uD83D\uDC24", "[:小鸡]" );		allIOSEmotionsMap.put("\uD83D\uDC14", "[:公鸡]" );
		allIOSEmotionsMap.put("\uD83D\uDC27", "[:企鹅]");		allIOSEmotionsMap.put("\uD83D\uDC1B", "[:毛毛虫]" ); 		allIOSEmotionsMap.put("\uD83D\uDC19", "[:章鱼]" );
		allIOSEmotionsMap.put("\uD83D\uDC20", "[:花斑鱼]");	allIOSEmotionsMap.put("\uD83D\uDC1F", "[:淡水鱼]" );		allIOSEmotionsMap.put("\uD83D\uDC33", "[:鲸鱼]" );
		allIOSEmotionsMap.put("\uD83D\uDC2C", "[:海豚]");		allIOSEmotionsMap.put("\uD83D\uDC90", "[:花束]" );		allIOSEmotionsMap.put("\uD83C\uDF38", "[:鲜花]" );
		allIOSEmotionsMap.put("\uD83C\uDF37", "[:花朵]");		allIOSEmotionsMap.put("\uD83C\uDF40", "[:叶子]" );		allIOSEmotionsMap.put("\uD83C\uDF39", "[:玫瑰]" );
		allIOSEmotionsMap.put("\uD83C\uDF3B", "[:向日葵]");	allIOSEmotionsMap.put("\uD83C\uDF3A", "[:小花]" );		allIOSEmotionsMap.put("\uD83C\uDF41", "[:枫叶]" );
		allIOSEmotionsMap.put("\uD83C\uDF43", "[:树叶]");		allIOSEmotionsMap.put("\uD83C\uDF42", "[:落叶]" );		allIOSEmotionsMap.put("\uD83C\uDF34", "[:椰树]" );
		allIOSEmotionsMap.put("\uD83C\uDF35", "[:仙人柱]");	allIOSEmotionsMap.put("\uD83C\uDF3E", "[:兰花]" );		allIOSEmotionsMap.put("\uD83D\uDC1A", "[:海螺]" );
	}
	
	/*******
	 * 删除zindex
	 * @param content
	 * @return
	 */
	public static final String removeZIndexCSSName(final String content){
		return content == null ? "" : content.replaceAll("(z-index)|(Z-INDEX)", "");
	}
	
	/**************
	 * 删除uftamb4编码 并替换处理ios表情
	 * @param content
	 * @return
	 */
	public static final String removeMobileUTF8MB4Emotions(final String content){
		if(StringUtils.isBlank(content)){
			return "";
		}		
		final Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\uf0af\ua0a5-\uf0af\udfff]", 
				Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE ) ;
		final Matcher emojiMatcher = emoji.matcher ( content ) ;
		if(! emojiMatcher.find()){
			return content;
		}
		StringBuffer sb = new StringBuffer();  
		do {
			String emotion = allIOSEmotionsMap.get(emojiMatcher.group(0));
			if(emotion == null){
				emotion = "[err]";
			}
			emojiMatcher.appendReplacement(sb, emotion);
		}while (emojiMatcher.find());
		emojiMatcher.appendTail(sb); 
		return sb.toString();
	}

	/**************
	 * 删除uftamb4编码
	 * @param content
	 * @return
	 */
	public static final String removeMobileUTF8MB4Nickname(final String nickname){
		if(StringUtils.isBlank(nickname)){
			return "";
		}
		//第一步过滤
		final Pattern emoji = Pattern.compile(
				"[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\uf0af\ua0a5-\uf0af\udfff]|[\uf09f\uA490-\uf09f\uA4ff]", 
				Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE ) ;
		final Matcher emojiMatcher = emoji.matcher ( nickname ) ;
		final String result = emojiMatcher.replaceAll("");
		if(result.length() < 1){
			return result;
		}
		//有个客户遇到这个问题\xF0\x9F\xA4\x94
		final byte[] bytes = result.getBytes();
		for (int i = 0; i < bytes.length; i++) 
		{
			if(i < bytes.length - 3 && bytes[i] == 0xF0
					&& bytes[i+1] == 0x9F
					&& bytes[i+2] == 0xA4
					&& bytes[i+3] == 0x94)
			{
				for (int j = 0; j < 4; j++) { 
					 bytes[i+j] = 0x30;
				 }
				 i += 3;
			}
		}
        return new String(bytes).replaceAll("0000", "");	        
	}
	
	public static final String removeMobileUTF8MB44AppliyField(final String v){
		if(StringUtils.isBlank(v)){
			return "";
		}
		//第一步过滤
		final Pattern emoji = Pattern.compile(
				"[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\uf0af\ua0a5-\uf0af\udfff]|[\uf09f\uA490-\uf09f\uA4ff]", 
				Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE ) ;
		final Matcher emojiMatcher = emoji.matcher ( v ) ;
		final String result = emojiMatcher.replaceAll("");
		if(result.length() < 1){
			return result;
		}
		//有个客户遇到这个问题\xF0\x9F\xA4\x94
		final byte[] bytes = result.getBytes();
		for (int i = 0; i < bytes.length; i++) 
		{
			if(i < bytes.length - 3 && bytes[i] == 0xF0
					&& bytes[i+1] == 0x9F
					&& bytes[i+2] == 0xA4
					&& bytes[i+3] == 0x94)
			{
				for (int j = 0; j < 4; j++) { 
					 bytes[i+j] = 0x30;
				 }
				 i += 3;
			}
		}
        return new String(bytes);	        
	}
	
	/********************
	 * 当字符过长时， 截断
	 * @param str
	 * @param maxLetters
	 * @return
	 */
	public static final String subStringWhenAccessLimit(final String str, final int maxLetters){
		return (str != null && str.length() > maxLetters) ? str.substring(0, maxLetters) : str;
	}
	
	/**************
	 * 过滤出正确的url
	 * @param urls
	 * @return
	 */
	public static final String[] filterValidUrls(String[] urls) 
	{
		if(ArrayUtils.isEmpty(urls)){
			return new String[0];
		}
		final List<String> result = new LinkedList<String>();
		for(final String url : urls){
			if(ValidatorUtil.URLValidate(url)){
				result.add(url.trim());
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	/*****************
	 * 过滤数字
	 * @param str
	 * @return
	 */	
	public static final String filterChineseDigitals(final String str){
		if(str != null && str.length() > 0){
			final StringBuilder sb = new StringBuilder("");
			for(final char ch : str.toCharArray()){
				if((ch >= '0' && ch <= '9')){
					sb.append(ch);
				}else{
					switch(ch)
					{
						case '０': sb.append('0'); break;
						case '１': sb.append('1'); break;
						case '２': sb.append('2'); break;
						case '３': sb.append('3'); break;
						case '４': sb.append('4'); break;
						case '５': sb.append('5'); break;
						case '６': sb.append('6'); break;
						case '７': sb.append('7'); break;
						case '８': sb.append('8'); break;
						case '９': sb.append('9'); break;
					}
				}
			}
			return sb.toString();
		}
		return "";
	}
	
	/************
	 * 替换中文的A-z和0-9
	 * @param str
	 * @return
	 */
	public static final String filterChineseDigitalsOrLetters(final String str){
		if(str != null && str.length() > 0){
			final StringBuilder sb = new StringBuilder("");
			for(final char ch : str.toCharArray()){
				final String ch2 = chineseDigitalLetterMap.get(String.valueOf(ch));
				if(ch2 != null){
					sb.append(ch2);
				}else{
					sb.append(ch);
				}
			}
			return sb.toString();
		}
		return "";
	}	
	
	/***************
	 * 删除HTTP头部
	 * @param s
	 * @return
	 */
	public static final String removeHTTPHeaderLetters(final String s){
		if(s == null){
			return s;
		}
		return s.replaceAll("(http://)|(HTTP://)|(http%3A%2F%2F)|(http%3a%2f%2f)|(HTTP%3A%2F%2F)|(HTTP%3a%2f%2f)", "");
	}
	
	public static final boolean isStartWithHttp(final String s){
		if(s == null){
			return false;
		}
		return s.startsWith("http") || s.startsWith("HTTP");
	}
	
	/*************
	 * 删除查询关键字中的sql关键字
	 * @param queryWords
	 * @return
	 */
	public static final String removeSQLOperationWords(final String queryWords){
		if(queryWords == null){
			return queryWords;
		}
		return queryWords.replaceAll("(EXECUTE)|(execute)|(delete)|(DELETE)"
				+ "|(select)|(SELECT)|(from)|(FROM)|(')|(concate)|(CONCATE)|(;)|(where)|(WHERE)"
				+ "|(order)|(ORDER)|(drop)|(DROP)|(alter)|(ALTER)|(create)|(CREATE)|(modify)|(MODIFY)",
				"");
	}
	
	public static final String removeBlankWords(final String queryWords){
		if(queryWords == null){
			return queryWords;
		}
		return queryWords.replaceAll("( )|(　)|(\t)|(\n)", "");
	}

	/*************
	 * 分隔字符串
	 * @param text
	 * @param divideChars
	 * @param needRemoveWhiteSpace
	 * @return
	 */
	public static final List<String> splitText2StringList(final String text, String divideChars, 
			final boolean needRemoveWhiteSpace)
	{
		final Set<String> result = HtmlUtil.splitText2StringSet(text, divideChars, needRemoveWhiteSpace);
		if(result.size() > 0) {
			return CollectionUtils.toList(result);
		}
		return new ArrayList<String>(0);
	}
	
	public static final Set<String> splitText2StringSet(final String text, String divideChars, 
			final boolean needRemoveWhiteSpace)
	{
		if(StringUtils.isBlank(text)){
			return new HashSet<String>();
		}
		if(StringUtils.isBlank(divideChars)){
			divideChars = DividerChar.SingleWells.chars;
		}
		final String[] array = text.split(divideChars);
		final Set<String> result = new HashSet<String>();
		for(String e : array){
			if(StringUtils.isNotBlank(e))
			{
				if(needRemoveWhiteSpace){
					e = e.replaceAll(HtmlUtil.WhiteSpaceRegWithoutBlanks, "").trim();
					if(StringUtils.isNotBlank(e)){
						result.add(e);
					}
				}else{
					result.add(e);	
				}				
			}
		}
		return result;
	}
	
	/**************
	 * 创建重复的窜
	 * @param ch
	 * @param length
	 * @return
	 */
	public static final String createDuplicateCharacters(final char ch, final int length){
		final StringBuilder sb = new StringBuilder("");
		for(int i=0; i<length; i++){
			sb.append(ch);
		}
		return sb.toString();
	}

	/**  
    * 编码  
    * @param bstr  
    * @return String  
    */    
   public static String encodeBase64(byte[] bstr){    
	   return new sun.misc.BASE64Encoder().encode(bstr);    
  }    
	   
   /**  
    * 解码  
    * @param str  
    * @return string  
    */    
   public static byte[] decodeBase64(String str){    
	   byte[] bt = null;    
	   try {    
	       sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();    
	       bt = decoder.decodeBuffer( str );    
	   } catch (Exception e) {    
	       e.printStackTrace();    
	   }    
       return bt;    
   }
   
   /*****************
    * 是否两者都是空串  或者 两者相同
    * @param str1
    * @param str2
    * @param isIngoreCase
    * @return
    */
   public static final boolean isStringEqualOREmpty(final String str1, final String str2, final boolean isIngoreCase)
   {
	   if (StringUtils.isBlank(str1) && StringUtils.isBlank(str2)){
		   return true;
	   }
	   if(isIngoreCase){
		   return str1 != null && str1.equalsIgnoreCase(str2);
	   }else{
		   return str1 != null && str1.equals(str2);
	   }
   }

	public static final Set<String> filterSaihuitongVideosByRegExpression(String content) {
		if(StringUtils.isBlank(content)){
			return new HashSet<String>(0);
		}
		final Set<String> list = new HashSet<String>();
		Pattern p = Pattern.compile(SAIHUITONG_VIDEO_REG);
		Matcher m = p.matcher(content);
		while (m.find()) {
			final String src = m.group(1);
			if(src != null){
				list.add(VideoUtil.checkMP4Url(src.trim()));
			}
		}
		return list;
	}

	public static final Set<String> filterSaihuitongAttachmentsByRegExpression(String content) {
		if(StringUtils.isBlank(content)){
			return new HashSet<String>(0);
		}
		final Set<String> list = new HashSet<String>();
		Pattern p = Pattern.compile(SAIHUITONG_ATTACHMENT_REG);
		Matcher m = p.matcher(content);
		while (m.find()) {
			final String src = m.group(1);
			if(src != null){
				list.add(src.trim());
			}
		}
		return list;
	}
	
	/**********
	 * 如果为空串， 则返回null
	 * @param str
	 * @return
	 */
	public static final String nullIfEmpty(final String str){
		return StringUtils.isBlank(str) ? null : str;
	}
   
	public static final void main(String[] args)
	{
//		final Map<String, Object> param = new HashMap<String, Object>();
//		param.put("page", 1);
//		param.put("pageSize", 10);
//		param.put("orderType", -1);
//		param.put("sortId", "0");
//		param.put("state", "-1");
//		param.put("title", "");
//		String param = "http://sss.163.com / ssd sedfs";
//		param = HtmlUtil.removeHTTPHeaderLetters(param.toLowerCase().replaceAll("．", ".")
//				.replaceAll(HtmlUtil.WhiteSpaceReg, ""));
//		final int index = param.indexOf('/');
//		if(index > 0){
//			param = param.substring(0, index);
//		}
	//	System.out.println(HtmlUtil.removeMobileUTF8MB4Nickname(new String(new byte[]{(byte)0xF0,(byte)0x9F,(byte)0xA4,(byte)0x94})));
//		System.out.println(Calendar.getInstance().get(Calendar.MONTH));
//		System.out.println(new BigDecimal(3.55555).setScale(2, BigDecimal.ROUND_DOWN));
//		System.out.println(new BigDecimal(3.555).setScale(2, BigDecimal.ROUND_HALF_UP));
//		System.out.println(new BigDecimal(3.5555).setScale(2, BigDecimal.ROUND_HALF_DOWN));
//		System.out.println(new BigDecimal(3.555).setScale(2, BigDecimal.ROUND_HALF_DOWN));

//		final Set<String> failedPhones = new HashSet<String>(); //失败的号码列表
//		System.out.println("  ……&……&8\tjyuy".replaceAll("\\s|\t|\r|\n", ""));
//		System.out.println(sign.subsftring(index+1, sign.length()));
//		final String aaa = "<p><img src=\"http://mmbiz.qpic.cn/mmbiz/LJ7WR9HIjILuHZI6mtlkFDbV5fVia0ZFDb6I7NAWiaOibMpehdUtrjguYvpGKkZFWdS6mqt05aqwXvwjfBHCetEpA/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1\" "
//				+ "data-tangram-ori-src=\"http://mmbiz.qpic.cn/mmbiz/LJ7WR9HIjILuHZI6mtlkFDbV5fVia0ZFDb6I7NAWiaOibMpehdUtrjguYvpGKkZFWdS6mqt05aqwXvwjfBHCetEpA/0?wx_fmt=jpeg\" " +
//				"style=\"width: auto ! important; visibility: visible ! important; height: auto ! important;\" data-ratio=\"0.5392354124748491\" data-w=\"\"/></p>";
//		
//		final List<String>  list = HtmlUtil.filterImageSrcsByRegExpression(aaa);
//		System.out.println("\t\n浙大玉泉校区周六12/26 杭州群山徒全程不等人、都轻装、不拍照自己就是领".replaceAll(WhiteSpaceReg, ""));
	//	System.out.println(HtmlUtil.filterSaihuitongImageSrcs("我摄影、我喜爱、我擅长<img src=\"http://img.saihuitong.com/8/img/13/150180799d2.jpg\">", 1, true));
//		System.out.println("【赛会通】【】【我的】得得得(【赛会通】)|(，退订TD)".replaceAll("(【([^】]*)】)|(，退订回复TD)", ""));
//		final String content = "<style>width:2200px;</style>公司地址：四川省成都市郫县犀浦镇国宁西";
		//System.out.println(HtmlUtil.getBrief(content, 200));
//		final String[] extraParams = "111-123-23".split("\\-");
//		Calendar cal = Calendar.getInstance();
//		cal.set(2016, Calendar.APRIL, 18, 20, 0, 0);
//		for(ChineseLanguageValue v :ChineseLanguageValue.values()){
//			System.out.print("2323\'\"2323".replaceAll("(\"|')", "`"));
//		}
		//System.out.println(127 & (~a));
	//	System.out.println(BigDecimal.valueOf(0.1*1.006).setScale(5, BigDecimal.ROUND_HALF_UP).toString());
		//System.out.println(BigDecimal.valueOf(0.9*1.006).setScale(5, BigDecimal.ROUND_HALF_UP).toPlainString());
//		System.out.println(EmotionUtil.emotion2Img("ww.saihuitong.com", removeMobileUTF8MB4Emotions("222a2sdwe\ud83c\udf4223r2vfefv\ud83d\udfff")));
//		System.out.println(ValidatorUtil.cardNoValidate("330682198302154014"));
//		System.out.println(ValidatorUtil.cardNoValidate("330682830215401X"));
//		System.out.println(ValidatorUtil.cardNoValidate("330682145302154014"));
//		ThirdPartStateParamDTO dto = new ThirdPartStateParamDTO(123, "/user/lofin_wewer_werw"
//				+ "",
//				PlatformSimpleType.Saihuitong, 3456);
//		final String s = dto.createStateParam();
//		System.out.println(s);
//		dto = ThirdPartStateParamDTO.parseStateParam(s);
//		System.out.println(dto.getId() + "-" + dto.getResaleShopUserId());
//		System.out.println(msg);
//		System.out.println(HtmlUtil.removeMobileUTF8MB44AppliyField("1398000012"));
		final String[] array = "#-1#01#2#31#4.3#1#2#s#e#-#w#-2#3r".split(DividerChar.SingleWells.chars);
		final List<String> result = new LinkedList<String>();
		for(final String e : array){
			if(StringUtils.isNumeric(e)){
				result.add(e);
				System.out.println("-"+e+"-");
			}
		}
	}

}