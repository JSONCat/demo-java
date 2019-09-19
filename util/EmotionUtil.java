package com.sas.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sas.core.constant.DomainConstant;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.dto.CDNMonitorDataCache;
import com.sas.core.service.EnvironmentService;
import com.sas.core.util.keywordsearch.SearchAlgorithm;

/************
 * 表情相关的util
 * @author zhuliming
 *
 */
public class EmotionUtil {

	//点赞的表情
	public static final String LikeEmotion = "[强]";
	
	/************
	 * 表情的路径前缀
	 */
	public static final String EmotionURLPRefix = "/assets/img/emojis";
	
	private static String[] allEmotion = {"[微笑]", "[撇嘴]", "[色]", "[发呆]", 
		"[得意]", "[流泪]", "[害羞]", "[闭嘴]", "[大哭]", "[尴尬]", "[发怒]", "[调皮]",
		"[呲牙]", "[惊讶]", "[难过]", "[酷]","[冷汗]", "[抓狂]", "[吐]", "[偷笑]", "[可爱]",
		"[白眼]", "[傲慢]", "[困]", "[惊恐]", "[流汗]", "[憨笑]", "[大兵]", "[奋斗]",
		"[咒骂]", "[疑问]", "[嘘]", "[晕]", "[折磨]", "[衰]", "[骷髅]", "[敲打]", "[再见]",
		"[擦汗]", "[抠鼻]", "[鼓掌]", "[糗大了]",	"[坏笑]", "[左哼哼]", "[右哼哼]", "[哈欠]",
		"[鄙视]", "[委屈]", "[快哭了]", "[阴险]", "[亲亲]", "[吓]", "[可怜]", "[菜刀]", 
		"[啤酒]", "[咖啡]", "[饭]", "[猪头]", "[玫瑰]", "[示爱]", "[蛋糕]", "[炸弹]", "[刀]", 
		"[便便]", "[礼物]", "[抱抱]", LikeEmotion, "[弱]", "[握手]", "[胜利]", "[抱拳]", "[勾引]", "[差劲]", "[no]", "[ok]", "[err]",
		"[:太阳]","[:下雨]","[:云朵]","[:雪人]","[:月亮]","[:闪电]","[:刮风]","[:大浪]","[:狐狸]","[:狗狗]","[:老鼠]","[:米老鼠]",
		"[:兔子]","[:狼]","[:青蛙]","[:老虎]","[:小熊]","[:灰熊]","[:小猪]","[:小羊]","[:野猪]","[:猴子]","[:小猴]","[:小马]",
		"[:奔跑]","[:骆驼]","[:小羊]","[:大象]","[:小蛇]","[:小鸟]","[:小鸡]","[:公鸡]","[:企鹅]","[:毛毛虫]","[:章鱼]","[:花斑鱼]",
		"[:淡水鱼]","[:鲸鱼]","[:海豚]","[:花束]","[:鲜花]","[:花朵]","[:叶子]","[:玫瑰]","[:向日葵]","[:小花]","[:枫叶]","[:树叶]","[:落叶]",
		"[:椰树]","[:仙人柱]","[:兰花]","[:海螺]"
	};

	
	private static String prefix="<img src=\"" ;
	private static String center = EmotionURLPRefix + "/qq/";
	private static String emotionExtention = ".gif";
	private static String suffix = emotionExtention + "\"/>";
	
	private static SearchAlgorithm img2EmotionSearchAlgorithm = null; //图片url到表情符号的转换算法	
	private static Map<String, String> img2EmotionMap = null;  //表情到图片下标算法
	
	private static Map<String, Integer> privateEmotion2ImgIndexMap = null;  //表情到图片下标算法
	
	private static final Map<String, Integer> getEmotion2ImgIndexMap(){
		if(privateEmotion2ImgIndexMap == null){
			final Map<String, Integer> tmpMap = new HashMap<String, Integer>();
			for(int i = 0; i < allEmotion.length; i ++){
				tmpMap.put(allEmotion[i], i);
			}
			privateEmotion2ImgIndexMap = tmpMap;
		}
		return privateEmotion2ImgIndexMap;
	}
	
	/***********
	 * 所有表情列表
	 * @param domain
	 * @return
	 */
	public static final List<BinaryEntry<String, String>> allEmotions(String domain)
	{
		final Map<String, Integer> emotion2ImgIndexMap = EmotionUtil.getEmotion2ImgIndexMap();
		//替换结果字符串
		final List<BinaryEntry<String, String>> list = new LinkedList<BinaryEntry<String, String>>();
		if(StringUtils.isBlank(domain)){
			domain = DomainConstant.StatisticResourceDomain;
		}
		for(final String e : allEmotion)
		{
			final Integer emotionIndex = emotion2ImgIndexMap.get(e);
			if(emotionIndex != null){
				list.add(new BinaryEntry<String, String>(e, "http://" + domain + center 
						+ String.valueOf(emotionIndex)+emotionExtention));
			}
		}
		return list;
	}
	/*****************
	 * 表情缩写转化成图片
	 * @param domain
	 * @param content
	 * @return
	 */
	public static List<String> emotion2Img(String domain, final List<String> contents){
		if(CollectionUtils.isEmpty(contents)){
			return new ArrayList<String>(0);
		}
		final List<String> result = new ArrayList<String>(contents.size());
		for(final String content : contents){
			emotion2Img(domain, content);
		}
		return result;
	}
		
	/************
	 * 将表情转成图片
	 * @param domain
	 * @param content
	 * @return
	 */
	public static String emotion2Img(String domain, String content)
	{
		if(StringUtils.isBlank(content)){
			return "";
		}
		if(StringUtils.isBlank(domain)){
			domain = DomainConstant.StatisticResourceDomain;
		}
		final Map<String, Integer> emotion2ImgIndexMap = EmotionUtil.getEmotion2ImgIndexMap();
		//替换结果字符串
		final String emotionImgPrefix = prefix + "http://" + domain +  center;
		final StringBuilder sb = new StringBuilder("");
		for(int i=0; i<content.length(); i++)
		{
			if(content.charAt(i) == '[')
			{
				final int endIndex = content.indexOf(']', i);
				if(endIndex > i)
				{
					final String emotionTag = content.substring(i, endIndex+1);
					final Integer emotionIndex = emotion2ImgIndexMap.get(emotionTag);
					if(emotionIndex != null){
						sb.append(emotionImgPrefix + String.valueOf(emotionIndex)+suffix);
						i = endIndex;
						continue;
					}
				}				
			}
			sb.append(content.charAt(i));
		}
		return sb.toString();
	}
	
	/***************
	 * 将图片转化成表情
	 * @param content
	 * @return
	 */
	public static String Img2Emotion(final String content){
		if(StringUtils.isBlank(content)){
			return "";
		}
		if(img2EmotionSearchAlgorithm == null){
			synchronized(EmotionUtil.class)
			{
				if(img2EmotionSearchAlgorithm == null)
				{
					img2EmotionMap = new HashMap<String, String>();
					img2EmotionSearchAlgorithm = new SearchAlgorithm(false);
					for(int i = 0; i < allEmotion.length; i ++){
						final String keyword = center + String.valueOf(i)+emotionExtention;
						img2EmotionMap.put(keyword, allEmotion[i]);
						img2EmotionSearchAlgorithm.addKeyword(keyword);
					}
					img2EmotionSearchAlgorithm.buildAlgorithm();
				}
			}
		}
		//解析字符串
		final StringBuilder sb = new StringBuilder("");
		for(int i=0; i<content.length(); i++)
		{
			if(content.charAt(i) == '<')
			{
				final int endIndex = content.indexOf('>', i);
				if(endIndex > i+6)//<img=>
				{
					final String emotionImage = content.substring(i, endIndex+1);
					final List<String> emotions = img2EmotionSearchAlgorithm.SearchKeyWords(emotionImage);
					if(CollectionUtils.isNotEmpty(emotions)){
						sb.append(img2EmotionMap.get(emotions.get(0)));
						i = endIndex;
						continue;
					}
				}				
			}
			sb.append(content.charAt(i));
		}
		return sb.toString();
	}	
	
	/*******************
	 * 生成点评界面的用户评论摘要信息
	 * @param content
	 * @param maxLength
	 * @return
	 */
	public static final String generateReplySummary(String content,
			final int maxLength, final EnvironmentService environmentService)
	{	
		final String domain = CDNMonitorDataCache.instance.getCssJSDomain(environmentService, null, null);
		content = EmotionUtil.Img2Emotion(content);
		content = HtmlUtil.getPlainTextByReplaceImgNode(content,  "【图片】", maxLength, false, false); //已经转化emotions， 故false
		return EmotionUtil.emotion2Img(domain, content);
	}
	
	/**************
	 * 是否是赞的表情
	 * @param content
	 * @return
	 */
	public static final boolean isLikeEmotion(String content)
	{
		if(content == null){
			return false;
		}
		content = content.replaceAll(HtmlUtil.HTMLWhiteSpaceReg, "");
		if(content.equals(LikeEmotion)){
			return true;
		}else if(content.length() > 100){
			return false;
		}
		final String imgSrc = HtmlUtil.filterFirstSimpleImageSrcsByRegExpression(content);
		return imgSrc != null && imgSrc.contains("/qq/66.gif");
	}
	
	public static void main(String[] args){
		final List<BinaryEntry<String, String>> list = EmotionUtil.allEmotions("st.saihuitong.com");
		for(final BinaryEntry<String, String> entry : list){
			System.out.println(entry.key + "=" + entry.value);
		}		
	}
}
