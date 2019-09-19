/**
 * 
 */
package com.sas.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.SasTemplatePropertyConstant.SasActivityType;
import com.sas.core.constant.SmsConstant.SmsContentType;
import com.sas.core.constant.SmsConstant.SmsTemplateType;
import com.sas.core.constant.SmsConstant.SmsUsage;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasSmsTemplate;
import com.sas.core.util.keywordsearch.SearchAlgorithm;
import com.sas.core.util.meta.UserUtil;

/**
 * @author Administrator
 *
 */
public class SmsUtil {

	protected static final Logger logger = Logger.getLogger(SmsUtil.class);//每个IP的反馈次数监控
	
	private static final LRUMap phoneSmsTimeCachePerHour = new LRUMap(10000); //每小时的短信次数的控制;
	
	/***************
	 * 判断每个IP是否反馈次数太多了次数
	 * @param request
	 * @return
	 */
	public static final synchronized boolean isPhoneSmsMaxLimitPerIP(final HttpServletRequest request)
	{
		final String ip = RequestUtil.getUserIPFromRequest(request);
		final Calendar cal = Calendar.getInstance();
		final String key = ip + "_" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY))
				+ "_" + cal.get(Calendar.MINUTE);
		final Long times = (Long)phoneSmsTimeCachePerHour.get(key);
		if(times != null){
			phoneSmsTimeCachePerHour.put(key, times + 1L);
			if(times >= 5){
				return true;
			}
		}else{
			phoneSmsTimeCachePerHour.put(key, 1L);
		}
		return false;
	}
	
	/***************
	 * 所有短信的敏感词
	 */
	private static SearchAlgorithm forbiddenKeywordSearchAlgorithm = null;

	/*************************************短信黑词***************************************************
	 * 获取所有黑词
	 * @return
	 */
	public static final SearchAlgorithm getForbiddenKeywordSearchAlgorithmEngine(){
		if(forbiddenKeywordSearchAlgorithm != null){
			return forbiddenKeywordSearchAlgorithm;
		}
		synchronized(SmsUtil.class)
		{
			if(forbiddenKeywordSearchAlgorithm != null){
				return forbiddenKeywordSearchAlgorithm;
			}
			
			BufferedReader reader = null;
			try{
				final Set<String> allwords = new HashSet<String>();
				reader = new BufferedReader(new InputStreamReader(SasMenuActivityAddressTagUtil.class.getResourceAsStream("/core/sms_forbid_words.txt"), Encoding.UTF8.type));
				String line = null;
				while((line = reader.readLine()) != null){
					if(StringUtils.isNotBlank(line)){
						allwords.add(line.trim());
					}
				}
				forbiddenKeywordSearchAlgorithm = new SearchAlgorithm(true);
				forbiddenKeywordSearchAlgorithm.setKeywords(allwords.toArray(new String[allwords.size()]));
				forbiddenKeywordSearchAlgorithm.buildAlgorithm();
				logger.error("Success reading read forbidden keywords: count="+ allwords.size());	
			}catch(Exception ex){
				logger.error("Fail to read forbidden keywords: ex="+ex.getMessage(), ex);	
			}finally{
				IOUtil.closeReaderWithoutException(reader);
			}
		}
		return forbiddenKeywordSearchAlgorithm;		
	}
	
	/**************
	 * 删除并替换黑词
	 * @param content
	 * @return
	 */
	public static final String filterForbiddenKeywords(String content){
		//过滤黑词
		final List<String> forbiddenWords = SmsUtil.getForbiddenKeywordSearchAlgorithmEngine().SearchKeyWords(content);
		if(forbiddenWords.size() > 0){
			for(final String word : forbiddenWords){
				content = content.replaceAll(word, "");
			}
		}
		return content;
	}
	/*********************
	 * 生成订单的随机ordercode, 尾部加七位数字
	 * @return
	 */
	public static final String generateOrdeCode(){
		final String time = TimeUtil.formatCurrentTime(TimeFormat.YYYYMMDDHHMMSS);
		final String randStr = String.valueOf(RandomUtils.nextInt(999999) + 1000000).substring(1);
		return time + randStr;
	}
	
	/***********
	 * 创建消息
	 * @param content
	 * @return
	 */
	public static final String createSmsMsgContent(final Sas sas, String content, final SmsContentType contentType){
		content = content.replaceAll("【", "[").replaceAll("】", "]");
		final StringBuilder result = new StringBuilder("");
		if(StringUtils.isBlank(sas.getPhoneMsgSign())){
			result.append("【赛会通】");
		}else{
			result.append("【"+sas.getPhoneMsgSign()+"】");
		}
		for(final char ch : content.toCharArray()){
			if(ch == '\t' || ch == '\r' || ch == '\n'){
				continue;			
			}else if(ch == '【' || ch == '】'){
				result.append('"');
			}else if(ch == '｛'){
				result.append('{');
			}else if (ch == '｝'){
				result.append('}');				
			}else{
				result.append(ch);				
			}
		}		
		if(contentType == SmsContentType.MarketingPromotion){
			result.append(" 回T退订");
		}
		return result.toString();
	}
	
	public static final String createSmsValidationCodeMsgContent(final Sas sas, String content){
		if(StringUtils.isBlank(sas.getPhoneMsgSign())){
			return "【赛会通】" + content;
		}else{
			return "【"+sas.getPhoneMsgSign()+"】" + content;
		}		
	}
	
	/**************
	 * 过滤不合理的内容， 例如重复的签名等
	 * @param content
	 * @return
	 */
	public static final String filterContentInvalidChars(String content){
		if(content == null){
			return "";
		} else{
			content = content.replaceAll("(【([^】]*)】)|((，)*退订回复TD)|((,)*退订回复TD)|((，)*退订TD)|((,)*退订TD)", "");
			return content.replaceAll("\n|\t|\r", " ");
		}
	}
	
	/*************
	 * 获取短信条数
	 * @param content
	 * @return
	 */
	public static final int getSmsMsgCount(final String content){
		final int random = content.length() % 67 ;
		return (int)(content.length() / 67) + (random > 0 ? 1 : 0);
	}
	
	/*********************
	 * 计算短信的收费， 按阶梯收费
	 * @param smsCount
	 * @return
	 */
	public final static BigDecimal calculateSmsMoney(final long smsCount)
	{
		double money = 0;
		if(smsCount < 100){
			money = 0.095 * smsCount;
		}else if(smsCount < 500){
			money = 0.085 * smsCount;
		}else if(smsCount < 1000){
			money = 0.080 * smsCount;
		}else{
			money = 0.075 * smsCount;
		}
		return BigDecimal.valueOf(money).setScale(2, BigDecimal.ROUND_DOWN);			
	}
	
	/***************
	 *  2018年8月1日 至 2018年8月31日 充值赠送
	 *  充值100-499条，赠送5%；
	 *  充值500-999条，赠送10%；
	 *  充值1000-5999条，赠送15%
   *  充值6000条以上，赠送20%
	 * @param buySmsCount
	 * @return
	 */
	public static final int calculateSmsGiftCount(final int buySmsCount){
		return 0;
//		if(buySmsCount < 100){
//			return 0;
//		}else if(buySmsCount < 500){
//			return (int)(buySmsCount * 0.05);
//		}else if(buySmsCount < 1000){
//			return (int)(buySmsCount * 0.10);
//		}else if (buySmsCount < 6000){
//			return (int)(buySmsCount * 0.15);
//		}else{
//			return (int)(buySmsCount * 0.20);
//		}
	}
	
	/******************
	 * 将数据库的手机文件分割成功一个个手机
	 * @param phones
	 * @return
	 */
	public static final List<String> splitPhonesWithValidation(final String phones, String divideChars)
	{
		if(StringUtils.isBlank(phones)){
			return new ArrayList<String>(0);
		}
		if(StringUtils.isBlank(divideChars)){
			divideChars = DividerChar.SingleWells.chars;
		}
		final Set<String> result = new HashSet<String>();
		final String[] array = phones.split(divideChars);
		for(String phone : array){
			phone = UserUtil.processUserPhone(phone);
			if(ValidatorUtil.mobileValidate(phone)){
				result.add(phone);
			}
		}
		return CollectionUtils.toList(result);
	}
	
	/******************
	 * 对手机号组装成一段文本
	 * @param phones
	 * @param newDivider
	 * @return
	 */
	public static final String concatPhones(final List<String> phones)
	{
		if(CollectionUtils.isEmpty(phones)){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");
		for(final String phone : phones){
			if(sb.length() > 0){
				sb.append(DividerChar.SingleWells.chars);
			}
			sb.append(phone);
		}
		return sb.toString();
	}
	
	/**
	 * 参数化的短信内容验证
	 * @param cellPhone
	 * @return
	 */
	public static final boolean parameterSmsValidate(final String content) {
		if(StringUtils.isBlank(content)){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(".*\\{[^\\{\\}]+\\}.*");
			Matcher matcher = regex.matcher(content);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	/************
	 * 系统提供的短信模板
	 */
	public static final List<SasSmsTemplate> getDefaultSystemSmsTemplate(final Sas sas, final SasActivityType type,
			final boolean hasGoodMenus, final boolean hasActivityMenu, final boolean hasActivityScoreMenu)
	{
		final long now = TimeUtil.getMiliseconds(2016, 8, 1);
		final List<SasSmsTemplate> list = new ArrayList<SasSmsTemplate>(4);
		
		list.add(new SasSmsTemplate(SmsUsage.UserMarketingSms.id, 0L, 0, 
				SmsUsage.UserMarketingSms.getTemplateContent(sas, type),
				SmsTemplateType.FixContentSms, SmsUsage.UserMarketingSms, now));
		
		if(hasActivityMenu){
			list.add(new SasSmsTemplate(SmsUsage.ActivitySms.id, 0L, 0, 
					SmsUsage.ActivitySms.getTemplateContent(sas, type),
					SmsTemplateType.ParamSms, SmsUsage.ActivitySms, now));
			
			if(hasActivityScoreMenu){
				list.add(new SasSmsTemplate(SmsUsage.MatchPlayerSms.id, 0L, 0, 
					SmsUsage.MatchPlayerSms.getTemplateContent(sas, type),
					SmsTemplateType.ParamSms, SmsUsage.MatchPlayerSms, now));
				list.add(new SasSmsTemplate(SmsUsage.MatchPlayerScoreSms.id, 0L, 0, 
						SmsUsage.MatchPlayerScoreSms.getTemplateContent(sas, type),
						SmsTemplateType.ParamSms, SmsUsage.MatchPlayerScoreSms, now));
			}
		}
		
		if(hasGoodMenus){
			list.add(new SasSmsTemplate(SmsUsage.GoodSms.id, 0L, 0, 
				SmsUsage.GoodSms.getTemplateContent(sas, type),
				SmsTemplateType.ParamSms, SmsUsage.GoodSms, now));
		}
		
		return list;
	};
	
	/***************
	 * 解析生成手机短信内容， 可能短信是模板， 需要动态生成
	 * @param phoneDetail
	 * @param templateContent
	 * @return
	 */
	public static final BinaryEntry<String, String> parseAndGenerateSmsPhoneContentEntry(String phoneDetail, 
			String templateContent)
	{
		phoneDetail = phoneDetail == null ? null : phoneDetail.trim();
		templateContent = templateContent == null ? null : templateContent.trim();
		if(phoneDetail == null || phoneDetail.length() < 5 || templateContent == null || templateContent.length() < 1){
			return null;
		}
		final int index = phoneDetail.indexOf('{');
		if(index < 0){
			return new BinaryEntry<String, String>(HtmlUtil.filterChineseDigitals(phoneDetail), templateContent);
		}
		if(index < 1 || index == (phoneDetail.length() - 1)){
			return new BinaryEntry<String, String>(HtmlUtil.filterChineseDigitals(phoneDetail), templateContent);
		}
		//解析出参数
		final String params[] = phoneDetail.substring(index).split("(,|，|\\{|\\})");
		final Map<String, String> allParams = new HashMap<String, String>();
		for(String param : params){
			param = param.trim();
			if(param.length() < 1){
				continue;
			}
			final String[] entries = param.split("=");
			if(entries.length < 2){
				allParams.put(entries[0].trim().toLowerCase(), "");
			}else{
				allParams.put(entries[0].trim().toLowerCase(), entries[1]);
			}
		}
		//替换成参数
		String paramName = null;
		final StringBuilder resultSms = new StringBuilder("");
		for(final char ch : templateContent.toCharArray()){
			if(ch == '{'){
				if(paramName != null){
					resultSms.append("{" + paramName);
				}
				paramName = "";
			}else if(ch == '}'){
				if(paramName == null){
					resultSms.append(ch);
				}else if(paramName.length() < 1){
					resultSms.append("{}");
				}else{
					final String v = allParams.get(paramName.trim().toLowerCase());
					if(v == null){
						resultSms.append("{"+paramName+"}");
					}else{
						resultSms.append(v);
					}
				}
				paramName = null;
			}else{
				if(paramName != null){
					paramName = paramName + ch;
				}else{
					resultSms.append(ch);
				}
			}
		}
		if(paramName != null){
			resultSms.append("{"+paramName);
		}
		return new BinaryEntry<String, String>(phoneDetail.substring(0, index), resultSms.toString());
	}
	
	public static void main(String[] agrs){
		String aaa = "【323424】";
		System.out.println(aaa.replaceAll("【", "["));
	}
}
