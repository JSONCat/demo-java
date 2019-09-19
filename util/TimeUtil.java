/**
 * 
 */
package com.sas.core.util;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.sas.core.dto.MonthTimeArea;
import com.sas.core.dto.homepage.QuickEntryModuleData;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.exception.ServerUnknownException;

/**
 * 时间相关的util
 * @author zhuliming
 *
 */
public class TimeUtil {

	protected static final Logger logger = Logger.getLogger(TimeUtil.class);
	
	public static String todayYYYYMMDD = "";
	
	//12个月的时间戳范围
	private static final ConcurrentHashMap<Integer, MonthTimeArea> monthTimeMap = new ConcurrentHashMap<Integer, MonthTimeArea>();
	/*****************
	 * 2016.1.1时间值
	 */
	public static long Time20190101 = 0;
	public static long Time20180101 = 0;
	public static long Time20170101 = 0;
	public static long Time20160101 = 0;
	public static long Time20150101 = 0;
	public static long Time30000101 = 0;
	public static long TimeTodayStartTime = 0;
	public static long ServerStartTime = 0;
	static{
		final Calendar cal = Calendar.getInstance();
		ServerStartTime = cal.getTimeInMillis();
		todayYYYYMMDD = TimeUtil.formatDate(cal.getTime(), TimeFormat.yyyy_MM_dd);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		TimeTodayStartTime = cal.getTimeInMillis();
		cal.set(Calendar.YEAR, 3000);
		cal.set(Calendar.MONDAY, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Time30000101 = cal.getTimeInMillis();		
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONDAY, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Time20150101 = cal.getTimeInMillis();
		cal.set(Calendar.YEAR, 2016);
		cal.set(Calendar.MONDAY, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Time20160101 = cal.getTimeInMillis();
		cal.set(Calendar.YEAR, 2017);
		cal.set(Calendar.MONDAY, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Time20170101 = cal.getTimeInMillis();
		cal.set(Calendar.YEAR, 2018);
		Time20180101 = cal.getTimeInMillis();
		cal.set(Calendar.YEAR, 2019);
		cal.set(Calendar.MONDAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Time20190101 = cal.getTimeInMillis();
	}
	
	/***************
	 * 获取当前的小时， 24小时制
	 * @return
	 */
	public static final int getCurrent24Hour(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public static final long getNowHourStartMiliseconds(){
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	/*****************
	 * 获取今天的开始时间
	 * @return
	 */
	public static final Calendar getTodayStartTime(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	//本周起始值
	public static final Calendar getThisWeekStartTime(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		int weekDay = cal.get(Calendar.DAY_OF_WEEK);//1周日，2周一，3周二。。。。。， 不符合中国人的习惯
		if(weekDay == 1){
			weekDay = 8;
		}
		if(weekDay > 2){
			cal.add(Calendar.DAY_OF_YEAR, 2-weekDay);
		}
		return cal;
	}
	
	//本月
	public static final Calendar getThisMonthStartTime(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		final int day = cal.get(Calendar.DAY_OF_MONTH);
		if(day > 1){
			cal.add(Calendar.DAY_OF_YEAR, 1-day);
		}
		return cal;
	}	
	
	/********
	 * 获取今日的yyyy-mm-dd字符串
	 * @return
	 */
	public static final String getTodayYYYYMMDDString(){
		if(todayYYYYMMDD == null || todayYYYYMMDD.length() < 1){
			todayYYYYMMDD = TimeUtil.formatDate(Calendar.getInstance().getTime(), TimeFormat.yyyy_MM_dd);
		}
		return todayYYYYMMDD;
	}
	
	public static final Calendar getOneDayStartTime(final long timeInMiliseconds){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeInMiliseconds);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	/******************
	 * 获取今天的截止时间
	 * @return
	 */
	public static final Calendar getTodayEndTime(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal;
	}
	
	public static final Calendar getOneDayEndTime(final long timeInMiliseconds){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeInMiliseconds);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal;
	}
	
	/**
	 * @Title: getFirstDayOfWeek
	 * @Description: 获取这周第一天的时间
	 * @return
	 * @throws
	 */
	public static final Calendar getFirstDayOfWeek(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	/**
	 * @Title: getFirstDayOfMonth
	 * @Description: 获取本月第一天的时间
	 * @return
	 * @throws
	 */
	public static final Calendar getFirstDayOfMonth(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	/*************
	 * 格式化当前时间
	 * @param format
	 * @return
	 */
	public static final String formatCurrentTime(final TimeFormat format){
		if(format == null){
			return formatDateByDefaultFormat(Calendar.getInstance().getTime());
		}
		SimpleDateFormat f = new SimpleDateFormat(format.format);
		return f.format(Calendar.getInstance().getTime());
	}
	
	/**
	 * 把日期转化成指定格式的日期字符串
	 * 
	 * @param date
	 * @return
	 */
	public static final String formatDate(Date date, final TimeFormat format){
		if(format == null){
			return formatDateByDefaultFormat(date);
		}
		SimpleDateFormat f = new SimpleDateFormat(format.format);
		return f.format(date);
	}
	
	/*************
	 * 格式化成"yyyy-MM-dd HH:mm:ss"
	 * @param date
	 * @return
	 */
	public static final String formatDateByDefaultFormat(Date date) {
		if (date == null) {
			return "";
		}
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeFormat.yyyy_MM_dd_HH_mm_ss.format);
		return simpleDateFormat.format(date);
	}
	
	/*************
	 * 格式化成"yyyy/MM/dd HH:mm"
	 * @param date
	 * @return
	 */
	public static final String formatDateByDefaultFormat2(long time) {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeFormat.yyyy_MM_dd_HH_mm2.format);
		return simpleDateFormat.format(time);
	}
	
	/**
	 * 格式化日期
	 * 
	 * @param time
	 * @return
	 */
	public static final String formatDate(long time, final TimeFormat timeFormat){
		if(timeFormat == null){
			return formatDateByDefaultFormat(time);
		}
		SimpleDateFormat f = new SimpleDateFormat(timeFormat.format);
		return f.format(time);
	}

	/*************
	 * 格式化成"yyyy-MM-dd HH:mm:ss"
	 * @param date
	 * @return
	 */
	public static final String formatDateByDefaultFormat(long time) {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeFormat.yyyy_MM_dd_HH_mm_ss.format);
		return simpleDateFormat.format(time);
	}
	
	/**
	 * 按照指定格式转化时间， 返回对应的毫秒数
	 * @param dateStr  日期字符串
	 * @param format	转化格式
	 * @return
	 * @throws ParseException 
	 */
	public static final long parseDate2Miliseconds(String dateStr, TimeFormat timeFormat) {
		final Date date = parseDate(dateStr, timeFormat);
		return date.getTime();
	}
	
	public static final long parseDate2Miliseconds(String dateStr, TimeFormat timeFormat, final long defaultValue) {
		try{
			final Date date = parseDate(dateStr, timeFormat);
			return date.getTime();
		}catch(Exception ex){
			logger.error("Fail to parseDate2Miliseconds : dateStr=" + dateStr);
			return defaultValue;
		}
	}
	
	/***************
	 * 对输入的yyyy-mm-dd进行判断， 错误的格式返回为null
	 * @param t
	 * @return
	 */
	public static final String formatAndCheckDateAsFormatYYYY_MM_dd(final String t){
		final String[] array = t == null ? null 
				: HtmlUtil.filterChineseDigitalsOrLetters(t).split("(\\-)|(\\.)|(年)|(月)|(日)|(．)|(\\－)|(/)|(／)|(,)|(，)");
		if(array == null || array.length < 2){
			return null;
		}
		final int year = IdUtil.convertToInteger(array[0].replaceAll("([^0-9])", ""), -1);
		if(year < 1800 || year > Calendar.getInstance().get(Calendar.YEAR)+1){
			return null;
		}
		final int month = IdUtil.convertToInteger(array[1].replaceAll("([^0-9])", ""), -1);
		if(month < 1 || month > 12){
			return null;
		}
		int day = array.length < 3 ? 1 : IdUtil.convertToInteger(array[2].replaceAll("([^0-9])", ""), 1);
		if(day < 1){
			day = 1;
		}else if(month == 2 && day > 28){
			day = ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) ? 29 : 28;
		}else if(month != 2 && day > 30){
			day = (month == 4 || month == 6 || month == 9 || month == 11) ? 30 : 31;
		}
		return year + "-" + month + "-" + day;
	}
	
	public static final long formatAndCheckDateAsFormatYYYY_MM_dd(final String t, final long defaultValue){
		final String[] array = t == null ? null 
				: HtmlUtil.filterChineseDigitalsOrLetters(t).split("(\\-)|(\\.)|(年)|(月)|(日)|(．)|(\\－)");
		if(array == null || array.length < 2){
			return defaultValue;
		}
		final int year = IdUtil.convertToInteger(array[0].replaceAll("([^0-9])", ""), -1);
		if(year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)+1){
			return defaultValue;
		}
		final int month = IdUtil.convertToInteger(array[1].replaceAll("([^0-9])", ""), -1);
		if(month < 1 || month > 12){
			return defaultValue;
		}
		final int day = array.length < 3 ? 1 : IdUtil.convertToInteger(array[2].replaceAll("([^0-9])", ""), 1);
		if(day < 1 || day > 31){
			return defaultValue;
		}
		return TimeUtil.getMiliseconds(year, month, day);
	}
	
	/**
	 * 解析日期值字符串
	 * @param dateStr
	 * @return
	 * @throws ParseException 
	 * @throws EpayException
	 */
	public static final Date parseDate(String dateStr, TimeFormat timeFormat) {
		if (StringUtils.isBlank(dateStr) || timeFormat == null) {
			throw new ServerUnknownException("Fail to parseDate, error param: dateStr=" + dateStr + ", timeFormat=" + timeFormat);
		}
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat.format);		
		try {
			return simpleDateFormat.parse(dateStr);
		} catch (ParseException e) {
			throw new ServerUnknownException("Fail to parseDate with exception: dateStr=" + dateStr + ", timeFormat="
					+ timeFormat , e);
		}
	}
	
	public static final Date parseDateWithOutException(String dateStr, TimeFormat timeFormat) {
		try{
			return parseDate(dateStr, timeFormat);
		}catch(Exception ex){
			logger.error("Fail to parseDateWithOutException : dateStr=" + dateStr);
			return null;
		}
	}
	
	/******************
	 * 获取某一年的毫秒数
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static final long getMiliseconds(final int year, final int month, final int day)
	{
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, (month > 1) ? (month-1) : 0);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	public static final long getMiliseconds(final int year, final int month, final int day, final int hour24OfDay, final int minutes, final int seconds)
	{
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, (month > 1) ? (month-1) : 0);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour24OfDay);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, seconds);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	/*******************
	 * 重新设置时分秒
	 * @param time
	 * @param hours
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	public static final long resetTime(final long time, final int hours, final int minutes, final int seconds)
	{
		if(time <= Miliseconds.OneDay.miliseconds){
			return time;
		}
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, seconds);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	/*****************
	 * 计算两个时间之间的天数差额
	 * @param endTime
	 * @param startTime
	 * @return
	 */
	public static final int calculateBalanceDays(final long startTime, final long endTime)
	{
		if(endTime < 1 || startTime < 1 || endTime < startTime){
			return -1;
		}
		return (int)((endTime - startTime) / Miliseconds.OneDay.miliseconds);
	}
	
	/***************
	 * 是否时间差在一定范围内 
	 * @param time1
	 * @param time2
	 * @param range
	 * @return
	 */
	public static final boolean isTimeBalanceWithinRange(final long time1, final long time2, final long range)
	{
		long balance = time1 - time2;
		if(balance < 0){
			balance = -1 * balance;
		}
		return balance <= range;
	}
	
	/***********
	 * 计算年龄
	 * @param birthday
	 * @return
	 */
	public static final int calculateAge(final long birthday){
		final Calendar cal = Calendar.getInstance();
		final int year = cal.get(Calendar.YEAR);
		final int days = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTimeInMillis(birthday);
		int age = year - cal.get(Calendar.YEAR);
		if(cal.get(Calendar.DAY_OF_YEAR) < days){
			age --;
		}	
		return age;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		final Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.MONTH, Calendar.MARCH);
//		cal.set(Calendar.DAY_OF_MONTH, 31);
		final long now = System.currentTimeMillis();
//		System.out.println(TimeUtil.getOneDayStartTime(now).getTimeInMillis());
//		System.out.println(TimeUtil.getOneDayEndTime(now).getTimeInMillis());
//		System.out.println(Miliseconds.OneDay.miliseconds);
//		System.out.println((TimeUtil.getOneDayEndTime(now).getTimeInMillis()-TimeUtil.getOneDayStartTime(now).getTimeInMillis()));
		System.out.println(cal.get(Calendar.WEEK_OF_YEAR));
		System.out.println(TimeUtil.getOneDayStartTime(now).getTimeInMillis());
		System.out.println(TimeUtil.getOneDayEndTime(now).getTimeInMillis());
		System.out.println(Miliseconds.OneDay.miliseconds);
		System.out.println((TimeUtil.getOneDayEndTime(now).getTimeInMillis()-TimeUtil.getOneDayStartTime(now).getTimeInMillis()));
		System.out.println(formatAndCheckDateAsFormatYYYY_MM_dd("1984/2/18"));
		final List<QuickEntryModuleData> list = new ArrayList<QuickEntryModuleData>(2);
		QuickEntryModuleData d1 = new QuickEntryModuleData("http://www.saihuitong.com", "西湖夜跑1", 'M', "activity00", System.currentTimeMillis());
		QuickEntryModuleData d2 = new QuickEntryModuleData("http://www.saihuitong.com", "西湖夜跑2", 'I', "activity11", System.currentTimeMillis());
		list.add(d1);
//		list.add(d2);
		final String str = JsonUtil.getJsonString(list);
    System.out.println(str);
//		System.out.println(formatAndCheckDateAsFormatYYYY_MM_dd("1992 -1 0 -10"));
//		System.out.println(formatAndCheckDateAsFormatYYYY_MM_dd("1992 年 10月 10日"));
//		System.out.println(formatAndCheckDateAsFormatYYYY_MM_dd("1992-10"));
		//System.out.println(getMiliseconds(2060,2,3) - System.currentTimeMillis());
		System.out.println(14^3);
  }
  
  /**
	 * @Title: getFirstAndEndOfMonth
	 * @Description: 获取每月的首尾时间戳
	 * @return
	 * @throws
	 */
	public static final MonthTimeArea getFirstAndEndOfMonth(final int month){
		if(monthTimeMap.isEmpty() || monthTimeMap.size()<12){
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_YEAR, 1);
      for(int i=0 ; i<12 ; i++){
				MonthTimeArea monthTimeArea = new MonthTimeArea();
				cal.set(Calendar.MONTH, i );
				cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
				cal.set(Calendar.DAY_OF_MONTH,1);
				monthTimeArea.setStartTime(cal.getTimeInMillis());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);;
				monthTimeArea.setEndTime(cal.getTimeInMillis());
				monthTimeMap.put(i+1,monthTimeArea);
			}
		}
		if(month>12 && month <= 0){//错误月份
		  return null;
    }
		return monthTimeMap.get(month);
	}
}
