/**
 * 
 */
package com.sas.core.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.ActivityConstant.ActivityStyleTimeType;
import com.sas.core.constant.ActivityConstant.ApplyMethodType;
import com.sas.core.constant.CommonConstant.BinaryState;
import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.GoodConstant.GoodUnitType;
import com.sas.core.constant.GoodConstant.SaleSiteType;
import com.sas.core.constant.GoodConstant.ShelveState;
import com.sas.core.constant.SasConstant.SasSwitch;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TransactionConstant.OrderGoodType;
import com.sas.core.constant.UserConstant.ApplierPersonType;
import com.sas.core.constant.UserConstant.EducationLevel;
import com.sas.core.constant.UserConstant.IdentityCardType;
import com.sas.core.constant.UserConstant.SexType;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasMenuActivity;
import com.sas.core.meta.SasMenuActivityAdditionalService;
import com.sas.core.meta.SasMenuActivityAttachment;
import com.sas.core.meta.SasMenuActivityOrderApplier;
import com.sas.core.meta.SasMenuActivityStyleClass;
import com.sas.core.meta.SasMenuGood;
import com.sas.core.meta.SasMenuGoodSizeClass;
import com.sas.core.meta.SasMenuGoodStyleClass;
import com.sas.core.meta.SasMenuGoodStyleSizeRelation;
import com.sas.core.meta.SasUser;
import com.sas.core.meta.SasUserCartItem;
import com.sas.core.meta.SasUserStatistic;
import com.sas.core.service.SasMenuActivityOrderService;
import com.sas.core.util.meta.SasActivityUtil;
import com.sas.core.util.meta.SasUtil;

/**
 * 购物车UTIL
 * @author Administrator
 *
 */
public class UserCartUtil {

	public static final Logger logger = Logger.getLogger(UserCartUtil.class);
	
	/***********
	 * 是否需要重新调整计数
	 * @param sus
	 * @return
	 */
	public static final boolean needResetCartStatistic(final SasUserStatistic sus, final List<SasUserCartItem> items)
	{
		if(SasUtil.isLessThanZero(sus.getCartGoodsBuyCount()) 
				|| sus.getCartActivityApplierCount() < 0
				|| sus.getCartGoodsCount() < 0
				|| sus.getCartActivityCount() < 0
				|| items.size() != (sus.getCartGoodsCount() + sus.getCartActivityCount())){
			return true;
		}
		if(sus.getCartGoodsCount() == 0){
			if(!SasUtil.isZero(sus.getCartGoodsBuyCount())){
				return true;
			}
		}else if(SasUtil.isZero(sus.getCartGoodsBuyCount())){
			return true;			
		}
		if(sus.getCartActivityCount() == 0){
			if(sus.getCartActivityApplierCount() != 0){
				return true;
			}
		}else{
			if(sus.getCartActivityApplierCount() == 0){
				return true;
			}
		}
		return false;
	}

	/************
	 * 设置购物车的统计信息
	 * @param statistic
	 * @param resultMap
	 */
	public static void setUserCartSimpleStatistic(final SasUserStatistic statistic, final Map<String, Object> resultMap)
	{
		final long total = statistic.getCartActivityCount() + statistic.getCartGoodsCount();
		resultMap.put("totalCount", total);
		resultMap.put("totalActivityCount", statistic.getCartActivityCount());
		resultMap.put("totalProductCount", statistic.getCartGoodsCount());
		resultMap.put("totalItemCount", statistic.getCartGoodsBuyCount().add(new BigDecimal(statistic.getCartActivityApplierCount())).setScale(2, BigDecimal.ROUND_HALF_UP));
		resultMap.put("totalActivityApplyCount", statistic.getCartActivityApplierCount());
		resultMap.put("totalProductBuyCount", statistic.getCartGoodsBuyCount());
	}
	
	/****************
	 * 读取附件和解析出附件
	 * @param allApplyAttachments
	 * @return
	 */
	public static final String convert2AttachmentInfo(final SasMenuActivityAttachment[] allApplyAttachments)
	{
		if(ArrayUtils.isEmpty(allApplyAttachments)){
			return "";
		}
		final List<Map<String, String>> list = new ArrayList<Map<String, String>>(allApplyAttachments.length);
		for(final SasMenuActivityAttachment a : allApplyAttachments){
			if(StringUtils.isBlank(a.getUrl())){
				continue;
			}
			final Map<String, String> map = new HashMap<String, String>();
			map.put("url", a.getUrl());
			map.put("name", a.getName());
			map.put("size", String.valueOf(a.getSize()));
			list.add(map);
		}
		return JsonUtil.getJsonString(list);
	}
	
	public static final SasMenuActivityAttachment[] convertFromAttachmentInfo(final String str)
	{
		if(StringUtils.isBlank(str)){
			return new SasMenuActivityAttachment[0];
		}
		List<Map<String, String>> list = null;
		try{
			list = (List<Map<String, String>>)JsonUtil.getObject(str, List.class);
		}catch(Exception ex){
			logger.error("fail to convertFromAttachmentInfo:" + str, ex);
		}
		if(CollectionUtils.isEmpty(list)){
			return new SasMenuActivityAttachment[0];
		}
		final SasMenuActivityAttachment[] result = new SasMenuActivityAttachment[list.size()];
		int index = -1;
		for(final Map<String, String> map : list){
			result[++index] = new SasMenuActivityAttachment(map.get("name"),
					map.get("url"), IdUtil.convertTolong(map.get("size"), 0), System.currentTimeMillis());
		}
		return result;
	}

	/****************
	 * 读取团队报名信息和报名方式， 并解析出这些信息
	 * @param allApplyAttachments
	 * @return
	 */
	public static final String convert2ApplierInfo(final List<SasMenuActivityOrderApplier> appliers)
	{
		if(CollectionUtils.isEmpty(appliers)){
			return "";
		}
		final List<Map<String, String>> list = new ArrayList<Map<String, String>>(appliers.size());
		for(final SasMenuActivityOrderApplier a : appliers){
			final Map<String, String> map = new HashMap<String, String>();
			map.put("nickname", a.getNickname());//昵称
			map.put("trueName", a.getTrueName()); //真实姓名
			map.put("country", a.getCountry()); //国籍,国家名字
			map.put("sex", String.valueOf(a.getSex())); // '用户性别类型：M男性， F女性，U未知'
			map.put("bloodType", a.getBloodType()); //血型名字
			map.put("height", String.valueOf(a.getHeight())); // 身高（厘米）
			map.put("weight", String.valueOf(a.getWeight())); //体重（千克）
			map.put("birthdayYear", String.valueOf(a.getBirthdayYear())); //生日
			map.put("birthdayMonth", String.valueOf(a.getBirthdayMonth()));
			map.put("birthdayday", String.valueOf(a.getBirthdayday()));
			map.put("age", String.valueOf(a.getAge())); //年龄
			map.put("mobile", a.getMobile()); // '手机号'
			map.put("telephone", a.getTelephone()); // 固定电话	
			map.put("email", a.getEmail()); // '邮箱名'
			map.put("userIdentityCode", a.getUserIdentityCode()); // '证件码'
			map.put("userIdentityCardType", String.valueOf(a.getUserIdentityCardType())); // '证件类型：I-身份证,P-护照,H-港澳通行证,M-台胞证',
			map.put("emergencyContactPersonName", a.getEmergencyContactPersonName()); //  '紧急联系人真实姓名',
			map.put("emergencyContactPersonPhone", a.getEmergencyContactPersonPhone()); // '紧急联系人手机号',
			map.put("educationLevel", a.getEducationLevel()); //默认本科
			map.put("houseProvince", a.getHouseProvince()); //居住地
			map.put("houseCity", a.getHouseCity()); //居住地
			map.put("houseAddress", a.getHouseAddress()); //居住地
			map.put("mailProvince", a.getMailProvince()); //通信地址
			map.put("mailCity", a.getMailCity()); //通信地址
			map.put("mailAddress", a.getMailAddress()); //通信地址
			map.put("jobType", a.getJobType()); //职业
			map.put("monthlySalary", String.valueOf(a.getMonthlySalary()));//月薪
			map.put("company", a.getCompany()); //工作单位
			map.put("clothSize", a.getClothSize()); //服装尺码 =M：中码/S：小码/L：大码/XL：加大码/XXL：加加大码/XS：加小码/XXXL：3加大码/XXS：加加小码			
			map.put("privateFiledValues", a.getPrivateFiledValues());
			map.put("personType", a.getPersonType());
			list.add(map);
		}
		return JsonUtil.getJsonString(list);
	}
	
	public static final List<SasMenuActivityOrderApplier> convertFromApplierInfo(final String applierInfo,
			final boolean isEnglishLanguage)
	{
		if(StringUtils.isBlank(applierInfo)){
			return new ArrayList<SasMenuActivityOrderApplier>(0);
		}
		List<Map<String, String>> list = null;
		try{
			list = (List<Map<String, String>>)JsonUtil.getObject(applierInfo, List.class);
		}catch(Exception ex){
			logger.error("fail to convertFromApplierInfo:" + applierInfo, ex);
		}
		if(CollectionUtils.isEmpty(list)){
			return new ArrayList<SasMenuActivityOrderApplier>(0);
		}
		final List<SasMenuActivityOrderApplier> result = new LinkedList<SasMenuActivityOrderApplier>();
		for(final Map<String, String> map : list)
		{
			final SasMenuActivityOrderApplier applier = new SasMenuActivityOrderApplier();
			applier.setNickname(map.get("nickname")); //昵称			
			applier.setTrueName(map.get("trueName")); //真实姓名
			applier.setCountry(map.get("country")); //国籍,国家名字
			applier.setSex(SexType.parse(map.get("sex")).type);// '用户性别类型：M男性， F女性，U未知'
			applier.setBloodType(map.get("bloodType")); //血型名字
			applier.setHeight(IdUtil.convertToInteger(map.get("height"), 0)); // 身高（厘米）
			applier.setWeight(IdUtil.convertToInteger(map.get("weight"), 0)); //体重（千克）
			applier.setBirthdayYear(IdUtil.convertToInteger(map.get("birthdayYear"), 0)); //生日
			applier.setBirthdayMonth(IdUtil.convertToInteger(map.get("birthdayMonth"), 0));
			applier.setBirthdayday(IdUtil.convertToInteger(map.get("birthdayday"), 0));
			applier.setAge(IdUtil.convertToInteger(map.get("age"), 0)); //年龄
			applier.setMobile(map.get("mobile")); // '手机号'
			applier.setTelephone(map.get("telephone"));// 固定电话	
			applier.setEmail(map.get("email")); // '邮箱名'
			applier.setUserIdentityCode(map.get("userIdentityCode")); // '证件码'
			applier.setUserIdentityCardType(IdentityCardType.parse(map.get("userIdentityCardType")).type); // '证件类型：I-身份证,P-护照,H-港澳通行证,M-台胞证',
			applier.setEmergencyContactPersonName(map.get("emergencyContactPersonName"));//  '紧急联系人真实姓名',
			applier.setEmergencyContactPersonPhone(map.get("emergencyContactPersonPhone"));// '紧急联系人手机号',
			applier.setEducationLevel(StringUtils.defaultIfBlank(map.get("educationLevel"), EducationLevel.bachelor.name)); //默认本科
			applier.setHouseProvince(map.get("houseProvince")); //居住地
			applier.setHouseCity(map.get("houseCity")); //居住地
			applier.setHouseAddress(map.get("houseAddress"));; //居住地
			applier.setMailProvince(map.get("mailProvince")); //通信地址
			applier.setMailCity(map.get("mailCity")); //通信地址
			applier.setMailAddress(map.get("mailAddress")); //通信地址
			applier.setJobType(map.get("jobType")); //职业
			applier.setMonthlySalary(IdUtil.convertToInteger(map.get("monthlySalary"), 0));//月薪
			applier.setCompany(map.get("company"));//工作单位
			applier.setClothSize(map.get("clothSize"));//服装尺码 =M：中码/S：小码/L：大码/XL：加大码/XXL：加加大码/XS：加小码/XXXL：3加大码/XXS：加加小码			
			applier.setPrivateFiledValues(map.get("privateFiledValues"));
			applier.setPersonType(ApplierPersonType.parse(map.get("personType")).nameByLanguage(isEnglishLanguage));
			result.add(applier);
		}		
		return result;
	}
	
	/****************
	 * 读取联系人信息， 并解析出这些信息
	 * @param allApplyAttachments
	 * @return
	 */
	public static final String convert2ContactUserAndTeamInfo(final String contactTrueName, final String contactPhone, 
			final String teamApplierGroupName, final String teamApplierInfoUrl)
	{
		final Map<String, String> map = new HashMap<String, String>();
		map.put("contactTrueName", contactTrueName);
		map.put("contactPhone", contactPhone);
		map.put("teamApplierGroupName", teamApplierGroupName);
		map.put("teamApplierInfoUrl", teamApplierInfoUrl);
		return JsonUtil.getJsonString(map);
	}
	
	public static final String[] convertFromContactUserAndTeamInfo(final String contactUserInfo)
	{
		if(StringUtils.isBlank(contactUserInfo)){
			return new String[]{"", "", "", ""};
		}
		Map<String, String> map= null;
		try{
			map = (Map<String, String>)JsonUtil.getObject(contactUserInfo, Map.class);
		}catch(Exception ex){
			logger.error("fail to convertFromContactUserInfo:" + contactUserInfo, ex);
		}
		if(map == null || map.size() < 1){
			return new String[]{"", "", "", ""};
		}
		return new String[]{map.get("contactTrueName"),map.get("contactPhone"), map.get("teamApplierGroupName"),map.get("teamApplierInfoUrl")};
	}
	
	/***************
	 * 过滤出选择下单的那些购物车项目， 同时有必要的话更新数量
	 * @param items
	 * @param ids
	 * @param count
	 * @return
	 */
	public static final BinaryEntry<List<SasUserCartItem>, SasUserCartItem> filterSelectedItemAndSetCount(
			final SasUser sasUser, final List<SasUserCartItem> items,
			final long[] ids, final double[] count)
	{
		if(ArrayUtils.isEmpty(ids) || sasUser == null){
			return new BinaryEntry<List<SasUserCartItem>, SasUserCartItem>(new ArrayList<SasUserCartItem>(0), null);
		}
		final Map<Long, SasUserCartItem> itemMap = CollectionUtils.extractConllectionToMap(items, "id");
		final List<SasUserCartItem> selectedItems = new LinkedList<SasUserCartItem>();
		SasUserCartItem firstGoodItem = null;
		for(int i=0; i<ids.length; i++)
		{
			final SasUserCartItem item = itemMap.get(ids[i]);
			if(item != null && item.getUserId() == sasUser.getUserId() && item.getSasId() == sasUser.getSasId()){
				if(count != null && count.length > i && SasUtil.isGreaterThanZero(count[i])){
					item.setItemCount(new BigDecimal(count[i]).setScale(2, BigDecimal.ROUND_HALF_UP));
				}
				if(firstGoodItem == null && item.getItemType() == OrderGoodType.Good.type){
					firstGoodItem = item;
				}
				selectedItems.add(item);
			}			
		}
		return new BinaryEntry<List<SasUserCartItem>, SasUserCartItem>(selectedItems, firstGoodItem);
	}
	
	/**********
	 * 根据活动等信息确认购物车元素信息的正确性，如不一致， 则更新之
	 * @param item
	 * @param activity
	 * @param normalStyleClassesByTitle
	 * @param normalStyleClassesById
	 * @param servicesByTitle
	 * @param servicesMapById
	 * @param isEnglishLanguage
	 * @return
	 */
	public static final SasUserCartItem checkItemAccordingActivitysDetail(final Sas sas, final SasMenuActivityOrderService sasMenuActivityOrderService,
			final SasUserCartItem item, final SasMenuActivity activity,  final Map<String, SasMenuActivityStyleClass> normalStyleClassesByTitle, 
			final Map<Long, SasMenuActivityStyleClass> normalStyleClassesById, 
			final Map<String, SasMenuActivityStyleClass> calendarStyleClassMapByActivityIdAndStartTime,
			final Map<String, SasMenuActivityAdditionalService> servicesByTitle,
			final Map<Long, SasMenuActivityAdditionalService> servicesMapById, final boolean isEnglishLanguage,
			boolean useMemberPrice)
	{
		if(activity == null){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "event is deleted" : "活动已删除", BinaryState.No);
			return item;
		}
		UserCartUtil.setCartItemErrorState(item, (item.getApplyTeamType() == ApplyMethodType.Team.type ? 
				(isEnglishLanguage ? "Team apply" : "团队报名") 
				: (isEnglishLanguage ? "Personal apply" : "个人报名")), BinaryState.Yes); //设置正常状态
		item.setMenuId(activity.getMenuId());
		item.setItemTitle(activity.getTitle());
		item.setItemCover(activity.getCoverPicUrl());
		item.setUnit(activity.getPriceUnitName());
		item.setUnitType(GoodUnitType.SupportIntergal.type);
		item.setLogisticsFreeType(BinaryState.No.state);
		if(activity.getState() != ShelveState.On.state){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "event is off shelve" : "活动已下架", BinaryState.No);
		}else if(activity.getEndTime() < TimeUtil.TimeTodayStartTime){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "event closed" : "活动已结束", BinaryState.No);
		}else if(activity.getApplyExpireTime() < System.currentTimeMillis()){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "event apply expired" : "报名已截止", BinaryState.No);
		}else if(activity.getApplyStartTime() > System.currentTimeMillis()){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "apply later" : "报名未开始", BinaryState.No);
		}
		//确认批次 - 普通批次才进行确认
		double styleUnitPrice = -1, styleChildUnitPrice = -1; //批次单价
		if(item.getItemStyleClassId() > 0){
			SasMenuActivityStyleClass style = normalStyleClassesByTitle == null ? null : normalStyleClassesByTitle.get(item.getItemStyleClassTitle());
			if(style == null && normalStyleClassesById != null){
				style = normalStyleClassesById.get(item.getItemStyleClassId());
			}			
			if(style == null){
				if(normalStyleClassesByTitle != null || normalStyleClassesById != null){
					UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "batch group is off shelve" : "该批次已下架", BinaryState.No);
				}
			}else{
				long newStartTime = item.getItemStyleClassStartTime();
				long newEndTime = item.getItemStyleClassEndTime();
				String newTitle = item.getItemStyleClassTitle();
				final long end = (style.getEndTime() > 0) ? style.getEndTime() : activity.getEndTime();
				final long start = (style.getStartTime() > 0) ? style.getStartTime() : activity.getStartTime();
				if(style.getTimeType() == ActivityStyleTimeType.Once.type || newEndTime < TimeUtil.TimeTodayStartTime){
					if(end > item.getItemStyleClassEndTime()){
						newStartTime = start;
						newTitle = SasActivityUtil.formatStyleClassDisplayNameAndSetTime(activity, style);
						newEndTime = end;
					}
				}else if(StringUtils.isNotBlank(style.getTitle()) && end == newEndTime && start == newStartTime){
					newTitle = style.getTitle();
				}
				if(style.getStoreCount() < item.getItemCount().intValue()){
					if(style.getStoreCount() < 1){
						UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "batch group is sold out" : "批次已报满", BinaryState.No);
					}else{
						item.setLowStorage(true); //用户可以编辑减少报名人信息后再提交
						UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "low stocks" : "余位不足", null);
					}
				}else if((item.getItemStyleClassStartTime() - newStartTime) >= Miliseconds.OneDay.miliseconds
						|| (newEndTime - item.getItemStyleClassEndTime()) >= Miliseconds.OneDay.miliseconds){
					UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "event time changed" : "活动时间已更改", BinaryState.No);
				}else if(newEndTime < TimeUtil.TimeTodayStartTime){
					UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "event time expired" : "活动批次已过期", BinaryState.No);
				}				
				item.setItemStyleClassId(style.getId());
				item.setItemStyleClassTitle(newTitle);
				item.setItemStyleClassStartTime(newStartTime);
				item.setItemStyleClassEndTime(newEndTime);
				if(useMemberPrice && SasSwitch.SasOneUserMemberPriceLimit.isMe(sas.getSwitchState())){
					useMemberPrice = !sasMenuActivityOrderService.hasAppliedActivityStyleClassUsingMemberPrice(item.getSasId(),
							item.getUserId(), activity, style);
				}
				if(useMemberPrice){
				  if (style.getFixedAmount() == BinaryState.Yes.state){
				    styleUnitPrice = new BigDecimal(style.getPrice()).divide(item.getItemCount(), 2).doubleValue();
						styleChildUnitPrice = styleUnitPrice;
          }else {
				    styleUnitPrice = style.getMemberPrice();
					  styleChildUnitPrice = style.getMemberChildPrice();
          }
				}else{
					if (style.getFixedAmount() == BinaryState.Yes.state){
						styleUnitPrice = new BigDecimal(style.getPrice()).divide(item.getItemCount(), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
						styleChildUnitPrice = styleUnitPrice;
					}else {
						styleUnitPrice = style.getPrice();
						styleChildUnitPrice = style.getChildPrice();
					}
					
				}
			}
		}else{
			if(item.getItemStyleClassStartTime() < TimeUtil.TimeTodayStartTime){
				UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "event time expired" : "该批次已过期", BinaryState.No);
			}
			final SasMenuActivityStyleClass style = calendarStyleClassMapByActivityIdAndStartTime.get(item.getItemId() + "-" + item.getItemStyleClassStartTime());
			if(style != null){
				if(style.getState() != BinaryState.Yes.state){//有可能活动编辑的时候将之前批次删除了，所以不存在的话不做处理
					UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "batch group is off shelve" : "该批次已下架", BinaryState.No);
				}else if(style.getStoreCount() < item.getItemCount().intValue()){
					if(style.getStoreCount() < 1){
						UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "batch group is sold out" : "批次已报满", BinaryState.No);
					}else{
						item.setLowStorage(true); //用户可以编辑减少报名人信息后再提交
						UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "low stocks" : "余位不足", null);
					}
				}
				if(useMemberPrice && SasSwitch.SasOneUserMemberPriceLimit.isMe(sas.getSwitchState())){
					useMemberPrice = !sasMenuActivityOrderService.hasAppliedActivityStyleClassUsingMemberPrice(item.getSasId(),
							item.getUserId(), activity, style);
				}
				if(useMemberPrice){
					styleUnitPrice = style.getMemberPrice();
					styleChildUnitPrice = style.getMemberChildPrice();
				}else{
					styleUnitPrice = style.getPrice();
					styleChildUnitPrice = style.getChildPrice();
				}
				item.setItemStyleClassEndTime(style.getEndTime());
			}
		}
		//偶外服务更新
		int serviceTotalUnitPrice = 0; //服务单价
		if(StringUtils.isNotBlank(item.getItemStyleClass2Ids()))
		{
			final String[] serviceTitles = item.getItemStyleClass2Titles().split(DividerChar.ChineseComma.chars);
			final List<Long> serviceIdList = CollectionUtils.splitIdArray(item.getItemStyleClass2Ids(), DividerChar.Comma);
			final Long[] serviceIds = serviceIdList.toArray(new Long[serviceIdList.size()]);
			final List<Long> selectedServiceIds = new LinkedList<Long>();
			final List<String> selectedServiceTitles = new LinkedList<String>();
			boolean serviceIsFull = false;
			boolean serviceIsNotEnough = false;
			int deletedServiceCount = 0;			
			for(int i=0; i<serviceTitles.length && i<serviceIds.length; i++)
			{
				SasMenuActivityAdditionalService service = servicesByTitle == null ? null : servicesByTitle.get(serviceTitles[i]);
				if(service == null && servicesMapById != null){
					service = servicesMapById.get(serviceIds[i]);
				}
				if(service != null)
				{
					selectedServiceIds.add(service.getId());
					selectedServiceTitles.add(service.getTitle());
					if(service.getStoreCount() < item.getItemCount().intValue())
					{
						if(service.getStoreCount() < 1){
							serviceIsFull = true;
						}else{
							serviceIsNotEnough = true;
						}
					}
					serviceTotalUnitPrice += service.getPrice();
				}else{
					deletedServiceCount ++;
				}
			}
			if(deletedServiceCount < 1){//没有删除
				item.setItemStyleClass2Ids(CollectionUtils.joinStringArray(selectedServiceIds.toArray(new Long[selectedServiceIds.size()]), DividerChar.Comma));
				item.setItemStyleClass2Titles(CollectionUtils.joinStringArray(selectedServiceTitles.toArray(new String[selectedServiceTitles.size()]), DividerChar.ChineseComma));
			}else{
				if(serviceIsFull){
					UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "additional service is sold out" : "偶外服务已报满", BinaryState.No);
				}else if(serviceIsNotEnough){
					UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "additional service low stocks" : "偶外服务余位不足", null);
					item.setLowStorage(true);
				}
			}
		}
		if(styleUnitPrice >= 0){//说明批次存在
			item.setItemUnitPrice(new BigDecimal(styleUnitPrice + serviceTotalUnitPrice));
			if(styleChildUnitPrice < 0){
				styleChildUnitPrice = styleUnitPrice;
			}
			item.setItemChildUnitPrice(new BigDecimal(styleChildUnitPrice + serviceTotalUnitPrice));			
			if(item.getOrderDepositType() == BinaryState.Yes.state && SasUtil.isEqualORGreaterThanZero(activity.getDepositRatio())){//定金模式				
				item.setOrderDepositePrice(new BigDecimal(
								(MathUtil.minInt((int)styleUnitPrice, activity.getDepositRatio().intValue()) + serviceTotalUnitPrice) * (item.getItemCount().intValue() - item.getItemChildCount())
								+ (MathUtil.minInt((int)styleChildUnitPrice, activity.getDepositRatio().intValue()) + serviceTotalUnitPrice) * item.getItemChildCount()));
			}
		}
		if(item.getItemStyleClassEndTime() < TimeUtil.TimeTodayStartTime){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "style time expired" : "批次已过期", BinaryState.No);
		}
		return item;
	}
	
	private static final void setCartItemErrorState(final SasUserCartItem item, final String error, final BinaryState state)
	{
		if(state != null){
			//错误消息仅仅设置一次
			if(state == BinaryState.No && item.getValidState() == BinaryState.No.state){
				return;
			}
			item.setInvalidRemark(error);
			item.setValidState(state.state);
		}else{//仅仅设置错误消息
			if(item.getValidState() != BinaryState.No.state){
				item.setInvalidRemark(error);
			}
		}
	}
	
	/***********
	 * 根据商品等信息确认购物车元素信息的正确性，如不一致， 则更新之
	 * @param item
	 * @param good
	 * @param goodsStyleClassMap
	 * @param goodsSizeClassMap
	 * @param goodsStyleSizeClassMap
	 * @param isEnglishLanguage
	 * @return
	 */
	public static final SasUserCartItem checkItemAccordingGoodsDetail(final SasUserCartItem item,
			final SasMenuGood good, final Map<Long, SasMenuGoodStyleClass> styleClassMapById,
			final Map<String, SasMenuGoodStyleClass> styleClassMapByTitle,
			final Map<Long, SasMenuGoodSizeClass> sizeClassMapById,
			final Map<String, SasMenuGoodSizeClass> sizeClassMapByTitle,
			final Map<String, SasMenuGoodStyleSizeRelation> styleSizeRelationMap,
			final boolean isEnglishLanguage, final boolean saleIfNoStore, final boolean usingMemberPrice)
	{
		if(good == null){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "product is deleted" : "商品已删除", BinaryState.No);
			return item;
		}
		UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "under sale" : "销售中", BinaryState.Yes);//设置正常状态
		item.setMenuId(good.getMenuId());
		item.setItemTitle(good.getTitle());
		item.setItemCover(good.getCoverPicUrl());
		item.setUnit(good.getUnit());
		item.setUnitType(good.getUnitType());
		item.setLogisticsFreeType(good.getLogisticsFreeType());
		if(good.getState() != ShelveState.On.state){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "product is off shelve" : "商品已下架", BinaryState.No);
		}else if(good.getSaleSiteType() == SaleSiteType.ForeignSite.type){
			UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "product invalid" : "商品不支持购买", BinaryState.No);
		}
		//确认款式
		SasMenuGoodStyleClass style = styleClassMapByTitle == null ? null : styleClassMapByTitle.get(item.getItemStyleClassTitle());
		if(style == null && styleClassMapById != null){
			style = styleClassMapById.get(item.getItemStyleClassId());
		}
		if(style == null){
			if(styleClassMapByTitle != null || styleClassMapById != null){
				UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "style is off shelve" : "款式已下架", BinaryState.No);
			}
		} else{
			if(!saleIfNoStore){
				if(SasUtil.isLeftLessThanRight(style.getStoreCount(), item.getItemCount())){
					if(SasUtil.isEqualORLessThanZero(style.getStoreCount())){
						UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "sold out" : "款式已售罄", BinaryState.No);
					}else{
						//减少库存后再提交也是可以的
						item.setLowStorage(true);
						UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "low stocks: "+style.getStoreCount() : "库存不足: "+style.getStoreCount(), null);
					}
				}
			}
			item.setItemStyleClassId(style.getId());
			item.setItemStyleClassTitle(style.getTitle());	
		}
		//尺寸更新
		final long sizeClassId = StringUtils.isNotBlank(item.getItemStyleClass2Ids()) ? IdUtil.convertTolong(item.getItemStyleClass2Ids(), 0L) : 0L;
		//确认尺寸， 可能没有尺寸
		if(sizeClassMapById != null || sizeClassMapByTitle != null)
		{
			SasMenuGoodSizeClass sizeClass = sizeClassMapByTitle != null ? sizeClassMapByTitle.get(item.getItemStyleClass2Titles()) : null;
			if(sizeClass == null && sizeClassMapById != null){
				sizeClass = sizeClassMapById.get(sizeClassId);
			}
			if(sizeClass == null){
				if(sizeClassId > 0){
					UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "sold out" : "尺寸已下架", BinaryState.No);
				}
			}else{
				item.setItemStyleClass2Ids(String.valueOf(sizeClass.getId()));
				item.setItemStyleClass2Titles(sizeClass.getTitle());
			}
		}
		//确认库存
		final SasMenuGoodStyleSizeRelation relation = styleSizeRelationMap == null ? null
				: styleSizeRelationMap.get(item.getItemStyleClassId() + DividerChar.ComplexCharWithSemicolon.chars + sizeClassId);
		if(relation == null){
			if(styleSizeRelationMap != null){
				UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "style is off shelve" : "款式已变更", BinaryState.No);
			}
		}else {
			if(!saleIfNoStore){
				if(SasUtil.isLeftLessThanRight(relation.getStoreCount(), item.getItemCount()) && item.getValidState() == BinaryState.Yes.state){
					if(SasUtil.isEqualORLessThanZero(relation.getStoreCount())){
						UserCartUtil.setCartItemErrorState(item, isEnglishLanguage ? "sold out" : "款式已售罄", BinaryState.No);						
					}else{//容许减少数量再提交
						item.setLowStorage(true);
						UserCartUtil.setCartItemErrorState(item, 
								isEnglishLanguage ? "low stocks: "+ relation.getStoreCount(): "库存不足: "+relation.getStoreCount(), null);
					}
				}
			}
			item.setItemUnitPrice(usingMemberPrice ? relation.getPrice() : relation.getOriginalPrice());			
			item.setSkuCode(relation.getSkuCode());
		}
		return item;
	}
	
	/***************
	 * 按照类型进行分组: key为活动订单数量， value为商品订单数量
	 * @param orderTypes
	 * @return
	 */
	public static final BinaryEntry<Integer, Integer> calculateOrderCountGroupByOrderType(final int[] orderTypes)
	{
		if(orderTypes == null || orderTypes.length < 1){
			return new BinaryEntry<Integer, Integer>(0, 0);
		}
		int totalGoodsOrderCount = 0, totalActivityOrderCount = 0;
		for(final int orderType : orderTypes){
			if(orderType == OrderGoodType.Good.type){
				totalGoodsOrderCount ++;
			}else if(orderType == OrderGoodType.Activity.type){
				totalActivityOrderCount ++;
			}
		}
		return new BinaryEntry<Integer, Integer>(totalActivityOrderCount, totalGoodsOrderCount);
	}
}
