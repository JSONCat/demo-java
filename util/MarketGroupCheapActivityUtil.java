package com.sas.core.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.SortOrder;
import com.sas.core.constant.OrderConstant.OrderState;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.dto.SasMarketGroupCheapActivityDetail;
import com.sas.core.meta.SasMarketGroupCheapActivity;
import com.sas.core.meta.SasMarketGroupCheapPerson;
import com.sas.core.util.meta.SasUtil;

/**
 * 全民拼团的折扣处理
 * @author zhangjun
 *
 */
public class MarketGroupCheapActivityUtil {
	
	private static final Logger logger = Logger.getLogger(MarketGroupCheapActivityUtil.class);
	
	/************
	 * 重新设置尾款， 如尾款为0 则更新状态
	 * @param act
	 * @param persons
	 * @return
	 */
	public static final List<SasMarketGroupCheapPerson> resetPersonRetainageAndStates(final SasMarketGroupCheapActivity act,
			final List<SasMarketGroupCheapPerson> persons)
	{
		if(CollectionUtils.isNotEmpty(persons))
		{
			final BigDecimal currentPrice = MarketGroupCheapActivityUtil.getCurrentTotalPrice(act);
			for(final SasMarketGroupCheapPerson p : persons)
			{
				MarketGroupCheapActivityUtil.resetPersonRetainageAndState(act.getDepositPrice(), currentPrice, p);
			}
		}
		return persons;
	}
	
	private static final SasMarketGroupCheapPerson resetPersonRetainageAndState(final BigDecimal depositePrice,
			final BigDecimal currentPrice, final SasMarketGroupCheapPerson p)
	{
		if(p.getState() == OrderState.Success.state){//尾款已经支付了， 直接跳过
			return p;
		}else{					
			if(p.getState() == OrderState.UnPaid.state){
				p.setDepositPrice(depositePrice);						
			}
			final BigDecimal retainage = currentPrice.subtract(p.getDepositPrice());
			if(SasUtil.isEqualORLessThanZero(retainage.doubleValue())){
				p.setRetainage(new BigDecimal(0));
				if(p.getState() == OrderState.Paid.state){
					p.setState(OrderState.Success.state);
				}
			}else{
				p.setRetainage(retainage);
			}				
		}
		return p;
	}
	
	public static final SasMarketGroupCheapPerson resetPersonRetainageAndState(final SasMarketGroupCheapActivity act,
			final SasMarketGroupCheapPerson person)
	{
		return MarketGroupCheapActivityUtil.resetPersonRetainageAndState(act.getDepositPrice(), 
				MarketGroupCheapActivityUtil.getCurrentTotalPrice(act), person);
	}
	/***********
	 * 计算当前总价， 需要结合折扣
	 * @param activity
	 * @return
	 */
	public static final BigDecimal getCurrentTotalPrice(final SasMarketGroupCheapActivity activity)
	{		
		BigDecimal price = activity.getOriginalPrice();
		if(SasUtil.isGreaterThanZero(activity.getCurrentDiscount())){
			price = activity.getOriginalPrice().multiply(activity.getCurrentDiscount());
			price = price.divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP);
		}
		return price.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	/**
	 * 创建全民拼团参加者详情页链接
	 * @param actId 全民拼团ID
	 * @param personId 参加者ID
	 * @return
	 */
	public static final String createMarketGroupCheapActivityPersonDetailURL(final long actId, final long userId){
		return "/m/market/group/cheap/detail?id="+actId+"&fromUserId="+userId;
	}
	
	/********
	 * 折扣转成字符串
	 * @param binaryEntries
	 * @return
	 */
	public static final String convertDiscountSetting2String(final List<BinaryEntry<Integer, BigDecimal>> binaryEntries)
	{
		if(CollectionUtils.isEmpty(binaryEntries)){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");
		CollectionUtils.sortTheList(binaryEntries, "key", SortOrder.ASC);
		for(final BinaryEntry<Integer, BigDecimal> binaryEntry : binaryEntries){
			if(binaryEntry.getKey() < 0 || SasUtil.isLessThanZero(binaryEntry.getValue())){
				continue;
			}
			if(sb.length() > 0){
				sb.append(DividerChar.ComplexCharWithSemicolon2.chars);
			}
			sb.append(binaryEntry.getKey() +  DividerChar.Equal.chars + binaryEntry.getValue());
		}
		return sb.toString();
	}
	
	
	/************
	 * 读取折扣设置
	 * @param activity
	 * @return
	 */
	public static List<BinaryEntry<Integer, BigDecimal>> parseDiscountSettingFromString(final String setting) {
		if(StringUtils.isBlank(setting)){
			return new ArrayList<BinaryEntry<Integer, BigDecimal>>(0);
		}
		final String[] discounts = setting.split(DividerChar.ComplexCharWithSemicolon2.chars);
		final List<BinaryEntry<Integer, BigDecimal>> list = new LinkedList<BinaryEntry<Integer, BigDecimal>>();
		for(final String discount : discounts){
			final String[] array = discount.split(DividerChar.Equal.chars);
			if(array.length >= 2){					
				list.add(new BinaryEntry<Integer, BigDecimal>(IdUtil.convertToInteger(array[0], 0),
						new BigDecimal(IdUtil.convertToDouble(array[1], 10)).setScale(2, BigDecimal.ROUND_HALF_UP)));		
			}	
		}
		return list;
	}
	
	/**
	 * 折扣处理
	 * @param discountStore
	 * @param discount
	 * @return
	 */
	public static List<BinaryEntry<Integer, BigDecimal>> convert2BigDecimalArrays (int[] discountStore , double[] discount)
	{
		if(ArrayUtils.isEmpty(discountStore) || ArrayUtils.isEmpty(discount)){
			return new ArrayList<BinaryEntry<Integer, BigDecimal>>(0);
		}
		final List<BinaryEntry<Integer, BigDecimal>> binaryEntries = new ArrayList<BinaryEntry<Integer,BigDecimal>>(discountStore.length);
		for (int i = 0; i < discountStore.length&&i<discount.length; i++) {
			BinaryEntry<Integer, BigDecimal> binaryEntry = new BinaryEntry<Integer, BigDecimal>(discountStore[i],
					new BigDecimal(discount[i]).setScale(2, BigDecimal.ROUND_HALF_UP));
			binaryEntries.add(binaryEntry);
		}
		CollectionUtils.sortTheList(binaryEntries, "key", SortOrder.ASC);		
		return binaryEntries;	
	}
	
	/**
	 * 组团活动折扣梯价正确判断, 最低价不能低于定金， 出错的话返回错误信息， 否则返回null
	 * @param binaryEntries
	 * @return
	 */
	public static String getErrorIfDiscountSettingInValid(List<BinaryEntry<Integer, BigDecimal>> binaryEntries,
			final double oldDepositePrice, final double depositePrice, final double originalPrice, final int storeCount)
	{
		if(CollectionUtils.isEmpty(binaryEntries)){
			return "请填写正确的活动折扣梯价！";
		}
		if(depositePrice <0.0001 || originalPrice < 0.0001 || storeCount < 1){
			return "定金，原价和活动名额必须大于0！";
		}
		BigDecimal lastDiscount = null, minDiscount = null;
		int maxDiscountPersonCount = 0;
		for (BinaryEntry<Integer, BigDecimal>  entry  : binaryEntries) {
			if(entry.getKey() < 0 || SasUtil.isLessThanZero(entry.getValue())){
				return "折扣比例人数、折扣不能为负数!";
			}
			if(SasUtil.isGreaterThanZero(entry.getValue().doubleValue()-10)){
				return "填写正确的折扣，折扣数应小于10!";
			}
			if(lastDiscount == null){
				lastDiscount = entry.getValue();
			}else{
				if(SasUtil.isLeftLessThanRight(minDiscount, entry.getValue())){
					return "折扣比例随人数递增时，折扣必须递减!";
				}
			}
			minDiscount = entry.getValue();
			maxDiscountPersonCount = entry.getKey();
		}
		//人数判断
		if(maxDiscountPersonCount > storeCount){
			return "最大的活动折扣人数不能大于活动名额！";
		}
		//最低价不能低于定金
		final double minPrice = minDiscount.multiply(new BigDecimal(originalPrice)).divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
		if(SasUtil.isLeftLessThanRight(minPrice, depositePrice)){
			return "最小的活动折扣价不可小于定金！"+ minPrice;
		}
		if(SasUtil.isGreaterThanZero(oldDepositePrice) && SasUtil.isLeftLessThanRight(minPrice, oldDepositePrice)){
			return "该活动已有人报名，活动折扣最低价不可小于原定金！";
		}
		return null;
	}
	
	/**
	 * 达到下一折扣的信息
	 * @param cheapActivityDetail
	 * @return
	 */
	public static BinaryEntry<Integer, BigDecimal> parseNextDiscountFromString(final SasMarketGroupCheapActivityDetail cheapActivityDetail){
		Integer number = cheapActivityDetail.getCheapActivity().getTotalPaidUserCount();
		BigDecimal discount = new BigDecimal(0);
		List<BinaryEntry<Integer, BigDecimal>> binaryEntries = cheapActivityDetail.getDiscounts();
		for(BinaryEntry<Integer, BigDecimal>  entry : binaryEntries){
			if(cheapActivityDetail.getCheapActivity().getCurrentDiscount().doubleValue() > entry.getValue().doubleValue()){
				number = entry.getKey() - number ;
				discount = entry.getValue();
				break ;
			}
		}
		return new BinaryEntry<Integer, BigDecimal>(number, discount);
		
	}
}
