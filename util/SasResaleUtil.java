/**
 * 
 */
package com.sas.core.util;


import com.sas.core.constant.CommonConstant;
import com.sas.core.constant.CommonConstant.RecommendState;
import com.sas.core.constant.TimeConstant;
import com.sas.core.constant.UserConstant.WithdrawalAccount;
import com.sas.core.meta.*;
import com.sas.core.util.meta.SasUtil;


/**
 * 转售相关的util
 * @author zhuliming
 *
 */
public class SasResaleUtil {

	/**************
	 * 创建详情页的URL
	 * @param act
	 * @return
	 */
	public static final String createDetailURL(final SasResaleActivity act){
		return "/event/resale?id=" + act.getActivityId();
	}
	
	/************
	 * 创建详情页的URL
	 * @param h
	 * @return
	 */
	public static final String createDetailURL(final SasResaleActivityHistory h){
		return "/event/resale?id=" + h.getActivityId();
	}
	
	/**************
	 * 计算分销的成本
	 * @param styleClass
	 * @param applierCount
	 * @return
	 */
	public static final int calculateCost(final SasMenuActivityStyleClass styleClass, final int allApplierCount, final int applierChildCount){
		return styleClass.getSasResaleActivityStyleClassClosePrice() * (allApplierCount - applierChildCount)
				+ styleClass.getSasResaleActivityStyleClassCloseChildPrice() * applierChildCount;
	}
	
	/***********
	 * 生成分销商的收款账户信息
	 * @param resaleShop
	 * @return
	 */
	public static final String generateResaleShopWithdrawalAccountName(final SasUserResaleStatistic resaleShop)
	{
		if(resaleShop.getWithdrawalAccountType() == WithdrawalAccount.Bank.type){
			return "【" + resaleShop.getWithdrawalBankName().replaceAll("\\s|\t|\r|\n", "") 
					+ "】" + resaleShop.getWithdrawalBankUserName().replaceAll("\\s|\t|\r|\n", "")
					+ " " + resaleShop.getWithdrawalBankNum().replaceAll("\\s|\t|\r|\n", "") 
					+ " " + resaleShop.getWithdrawalBankDetail().replaceAll("\\s|\t|\r|\n", "") ;
		}else{
			return "【支付宝】" 
					+ resaleShop.getWithdrawalBankUserName().replaceAll("\\s|\t|\r|\n", "")
					+ " " + resaleShop.getWithdrawalBankNum().replaceAll("\\s|\t|\r|\n", "")  ;
		}	
	}
	
	/***********
	 * 生成排序的sort值
	 * @param activity
	 * @return
	 */
	public static final long generateActivitySort(final SasResaleActivity activity)
	{
		final long now = System.currentTimeMillis();
		if(activity.getRecommendState() == CommonConstant.RecommendState.Recommend.state){
			return SasUtil.generateNormalRecommendSort();//推荐 now+100年
		}
		return activity.getCreateTime();
	}
}
