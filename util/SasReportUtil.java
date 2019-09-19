/**
 * 
 */
package com.sas.core.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sas.core.constant.SasConstant.ApplySystemField;
import org.apache.commons.lang.ArrayUtils;

import com.sas.core.constant.ActivityScoreConstant.ScoreFieldProperty;
import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.OrderConstant.OrderState;
import com.sas.core.constant.PortalConstant.FinanceType;
import com.sas.core.constant.SasConstant.GoodOrderReportField;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.constant.TransactionConstant.OrderPayType;
import com.sas.core.constant.UserConstant.UserLevel;
import com.sas.core.constant.UserConstant.UserState;
import com.sas.core.dto.SasMenuGoodOrderItemDetail;
import com.sas.core.dto.SasReportGoodOrderRowDetail;
import com.sas.core.dto.SasUserReportDetail;
import com.sas.core.dto.SimpleActivityScoreDTO;
import com.sas.core.meta.Sas;
import com.sas.core.meta.SasMenuActivity;
import com.sas.core.meta.SasMenuActivityScoreGroup;
import com.sas.core.meta.SasMenuGoodOrder;
import com.sas.core.meta.SasMenuGoodOrderItem;
import com.sas.core.meta.SasMenuGoodStyleClass;
import com.sas.core.util.meta.SasGoodUtil;
import com.sas.core.util.meta.SasUtil;

/**
 * 报表相关的util
 * @author zhuliming
 *
 */
public class SasReportUtil {

	/**************
	 * 创建活动报表的副标题
	 * @param activity
	 * @param states
	 * @param classes
	 * @return
	 */
	public static final List<String> createActivityApplierSubTitles(final SasMenuActivity activity, final OrderState[] states,
			final String className)
	{
		//生成状态名字列表
		final StringBuilder stateString = new StringBuilder("");
		for(final OrderState state : states){
			if(state == OrderState.Deliverred){
				continue;
			}
			if(stateString.length() > 0){
				stateString.append(DividerChar.Comma.chars + " ");
			}
			stateString.append(state.name);
		}
		
		final List<String> titles = new ArrayList<String>(3);
		titles.add("分组批次：" + className);
		titles.add("订单状态：" + (states.length >= OrderState.values().length ? "全部" : stateString));
//		titles.add("导出日期：" + new SimpleDateFormat("yyyy-MM-dd HH: mm: ss").format(Calendar.getInstance().getTime()));
		return titles;
	}
	public static final List<String> createActivityApplierSubTitleCount(final Map<String, Integer> additionalServiceTitlesMap,
																								 											final Map<String, BigDecimal> exportTotalOrderPrice,
																																			final List<Long> fieldIdList){
	  final List<String> subTitles = new ArrayList<String>(3);
	  boolean selectedAdditionalService = false, selectedOrderTotalPrice = false;
	  if (CollectionUtils.isNotEmpty(fieldIdList)){
	    if (fieldIdList.contains(ApplySystemField.OrderAdditionalService.id)){
	      selectedAdditionalService = true;
      }
      if (fieldIdList.contains(ApplySystemField.OrderTotalPrice.id)){
	      selectedOrderTotalPrice = true;
      }
    }
    BigDecimal totalPrice = exportTotalOrderPrice.get("totalPrice");
    if (exportTotalOrderPrice.size() > 0){
      StringBuilder sb = new StringBuilder(exportTotalOrderPrice.size());
      for (Map.Entry<String, Integer> service: additionalServiceTitlesMap.entrySet()){
        if (sb.length() > 0){
          sb.append(DividerChar.Comma.chars);
        }
        sb.append(service.getKey() + "*" + service.getValue());
      }
      if (selectedAdditionalService){
       subTitles.add("附加服务: " + sb.toString());
      }
    }
    if (selectedOrderTotalPrice){
      subTitles.add("总价格: ￥" + (totalPrice == null ? 0 : totalPrice.toString()));
    }
    subTitles.add("导出日期：" + new SimpleDateFormat("yyyy-MM-dd HH: mm: ss").format(Calendar.getInstance().getTime()));
		return subTitles;
	}
	/**************
	 * 创建商品报表的副标题
	 * @param good
	 * @param states
	 * @param classes
	 * @return
	 */
	public static final List<String> createGoodOrderReportSubTitles(final OrderState[] states,
			final List<SasMenuGoodStyleClass> classes)
	{
		//生成状态名字列表
		final StringBuilder stateString = new StringBuilder("");
		for(final OrderState state : states){
			String name = state.name;
			if(state == OrderState.Deliverred){
				name = "已发货";
			}
			if(stateString.length() > 0){
				stateString.append(DividerChar.Comma.chars + " ");
			}
			stateString.append(name);
		}
		
		List<String> titles = null;
		if(CollectionUtils.isNotEmpty(classes)){
			titles = new ArrayList<String>(3);
			titles.add("选择款式：" + SasGoodUtil.joinClassNames(classes, DividerChar.Comma.chars+" "));
		}else{
			titles = new ArrayList<String>(2);
		}
		titles.add("订单状态：" + ((states == null || states.length >= OrderState.values().length) ? "全部" : stateString));
		titles.add("导出日期：" + new SimpleDateFormat("yyyy-MM-dd HH: mm: ss").format(Calendar.getInstance().getTime()));
		return titles;
	}
	
	/****************
	 * 获取字段对应的title列表, 用户报表显示
	 */
	public static final String[] generateBuyerFieldNames(final List<Long> fieldIdList, final int minColumnCount)
	{	
		final List<String> result = new LinkedList<String>();
		for(final Long fieldId: fieldIdList)
		{
			final GoodOrderReportField goodOrderReportField = GoodOrderReportField.parse(fieldId);
			if(goodOrderReportField != null){
				result.add(goodOrderReportField.fieldName);
			}
		}
		//如果列不够， 则放入空格
		while(minColumnCount > 0 && result.size() < minColumnCount){
			result.add("");
		}
		return result.toArray(new String[result.size()]);
	}
	
	/****************
	 * 按照字段id的顺序， 解析出字段的值， 每个数组代表一个报名人的字段信息
	 * @param appliers
	 * @param fieldIdList
	 * @param privateFieldMap
	 * @return
	 */
	public static final List<String[]> generateAllGoodBuyDetailsFieldValues(final List<SasMenuGoodOrderItemDetail> orderItems, 
			final List<Long> fieldIdList, final boolean needAddMoneySimbolPrefix)
	{
		if(CollectionUtils.isEmpty(orderItems) || CollectionUtils.isEmpty(fieldIdList)){
			return new ArrayList<String[]>(0);
		}
		final String moneyPrefix = needAddMoneySimbolPrefix ? "￥" : "";
		final List<String[]> result = new ArrayList<String[]>(orderItems.size());
		for(final SasMenuGoodOrderItemDetail item : orderItems)
		{
			final List<String> fieldValues = new ArrayList<String>(fieldIdList.size());
			for(final long fieldId : fieldIdList)
			{
				if(fieldId < 1){
					continue;
				}
				if(fieldId == GoodOrderReportField.OrderCreateTime.id){
					fieldValues.add(TimeUtil.formatDate(item.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm_ss));
				}else if(fieldId == GoodOrderReportField.OrderState.id){
					fieldValues.add(OrderState.parse(item.getState()).name);
				}else if(fieldId == GoodOrderReportField.OrderPayType.id){
					if(OrderState.hasPaid(item.getState())){
						fieldValues.add(OrderPayType.getTitle(item.getOrderPayType()));
					}else{
						fieldValues.add("");
					}
				}else if(fieldId == GoodOrderReportField.OrderTotalPrice.id){
					fieldValues.add(moneyPrefix+item.getTotalPrice());
				}else if(fieldId == GoodOrderReportField.OrderPayTime.id){
					if(item.getPayTime() > 0){
						fieldValues.add(TimeUtil.formatDate(item.getPayTime(), TimeFormat.yyyy_MM_dd_HH_mm));
					}else{
						fieldValues.add("");
					}
				}else if(fieldId == GoodOrderReportField.OrderCreditPointPrice.id){
					fieldValues.add(moneyPrefix+item.getUseCreditPointPrice().toString());
				}else if(fieldId == GoodOrderReportField.OrderCreditPointGiven.id){
					fieldValues.add(String.valueOf(item.getGetCreatePointCount()));
				}else if(fieldId == GoodOrderReportField.OrderStyleClass.id){//尺寸和规格
					fieldValues.add(item.getGoodStyleClassTitle() + " " + item.getGoodSizeClassTitle());
				}else if(fieldId == GoodOrderReportField.UserAddress.id){
					fieldValues.add(item.getUserProvince() + " " + item.getUserCity() + " " + item.getUserAddress());
				}else{
					final GoodOrderReportField field = GoodOrderReportField.parse(fieldId);
					if(field != null){
						Object value = SasUtil.readObjectProperty(item, field.propertyName, "");
						fieldValues.add(String.valueOf(value));
					}else{
						fieldValues.add("");
					}
				}
			}
			result.add( fieldValues.toArray(new String[fieldValues.size()]) );
		}
		return result;
	}
	
	/****************
	 * 按照字段id的顺序， 解析出字段的值， 每个数组代表一个报名人的字段信息
	 * @param appliers
	 * @param fieldIdList
	 * @param privateFieldMap
	 * @return
	 */
	public static final List<String[]> generateAllGoodBuyDetailsFieldValues(final List<SasMenuGoodOrder> orders,
			final List<SasMenuGoodOrderItem> orderItems,  final List<Long> fieldIdList, final boolean needAddMoneySimbolPrefix)
	{
		if(CollectionUtils.isEmpty(orderItems) || CollectionUtils.isEmpty(fieldIdList)){
			return new ArrayList<String[]>(0);
		}
		final Map<Long, List<SasMenuGoodOrderItem>> itemMapByOrder = CollectionUtils.convertListToMap(orderItems, "orderId");
		final List<String[]> result = new ArrayList<String[]>(orderItems.size());
		final int columnCount = fieldIdList.size() < 3 ? 3 : fieldIdList.size();
		for(final SasMenuGoodOrder order : orders)
		{			
			result.add(SasReportUtil.createGoodOrderRowValues(order, fieldIdList, needAddMoneySimbolPrefix));
			//添加商品信息
			final List<SasMenuGoodOrderItem> items = itemMapByOrder.get(order.getId());
			if(items == null || items.size() < 1){
				continue;
			}
			for(final SasMenuGoodOrderItem item : items){
				final String[] fieldValues = CollectionUtils.createStringArray(columnCount, "");
				fieldValues[0] = item.getGoodTitle();
				fieldValues[1] = item.getGoodSizeClassId() < 1 ? item.getGoodStyleClassTitle()
						: (item.getGoodStyleClassTitle() + " " + item.getGoodSizeClassTitle());
				fieldValues[2] = String.valueOf(item.getCount());
				result.add(fieldValues);
			}
		}
		return result;
	}
	
	public static final List<SasReportGoodOrderRowDetail> generateAllGoodBuyDetailsFieldValues2(final List<SasMenuGoodOrder> orders,
			final List<SasMenuGoodOrderItem> orderItems,  final List<Long> fieldIdList, final boolean needAddMoneySimbolPrefix)
	{
		if(CollectionUtils.isEmpty(orderItems) || CollectionUtils.isEmpty(fieldIdList)){
			return new ArrayList<SasReportGoodOrderRowDetail>(0);
		}
		final Map<Long, List<SasMenuGoodOrderItem>> itemMapByOrder = CollectionUtils.convertListToMap(orderItems, "orderId");
		final List<SasReportGoodOrderRowDetail> result = new LinkedList<SasReportGoodOrderRowDetail>();
		final int columnCount = fieldIdList.size() < 3 ? 3 : fieldIdList.size();
		for(final SasMenuGoodOrder order : orders)
		{		
			final String[] orderValues = SasReportUtil.createGoodOrderRowValues(order, fieldIdList, needAddMoneySimbolPrefix);
			//添加商品信息
			final List<SasMenuGoodOrderItem> items = itemMapByOrder.get(order.getId());
			if(items == null || items.size() < 1){
				result.add(new SasReportGoodOrderRowDetail(orderValues, new ArrayList<String[]>(0)));
				continue;
			}
			final List<String[]> orderItemValues = new ArrayList<String[]>(items.size());
			int i = 0;
			for(final SasMenuGoodOrderItem item : items){
				final String[] fieldValues = CollectionUtils.createStringArray(columnCount, "");
				fieldValues[0] = "【" + (++i) + "】" + item.getGoodTitle();
				fieldValues[1] = item.getGoodSizeClassId() < 1 ? item.getGoodStyleClassTitle()
						: (item.getGoodStyleClassTitle() + " " + item.getGoodSizeClassTitle());
				fieldValues[2] = String.valueOf(item.getCount());
				orderItemValues.add(fieldValues);
			}
			result.add(new SasReportGoodOrderRowDetail(orderValues, orderItemValues));
		}
		return result;
	}
	
	private static final String[] createGoodOrderRowValues(final SasMenuGoodOrder order, final List<Long> fieldIdList
			, final boolean needAddMoneySimbolPrefix)
	{
		final String moneyPrefix = needAddMoneySimbolPrefix ? "￥" : "";
		final int columnCount = fieldIdList.size() < 3 ? 3 : fieldIdList.size();
		final String[] fieldValues = CollectionUtils.createStringArray(columnCount, "");
		int i = -1;
		for(final long fieldId : fieldIdList)
		{
			i ++;
			if(fieldId < 1){
				continue;
			}
			if(fieldId == GoodOrderReportField.OrderCreateTime.id){
				fieldValues[i] = TimeUtil.formatDate(order.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm_ss);
			}else if(fieldId == GoodOrderReportField.OrderState.id){
				fieldValues[i] = OrderState.parse(order.getState()).name;
			}else if(fieldId == GoodOrderReportField.OrderPayType.id){
				if(OrderState.hasPaid(order.getState())){
					fieldValues[i] = OrderPayType.getTitle(order.getOrderPayType());
				}
			}else if(fieldId == GoodOrderReportField.OrderCouponDiscount.id){
				fieldValues[i] = order.getDiscountedPrice()==0 ? "" : (moneyPrefix + order.getDiscountedPrice());
			}else if(fieldId == GoodOrderReportField.OrderTotalPrice.id){
				fieldValues[i] = moneyPrefix+order.getTotalPriceWithRefundAmount();
			}else if(fieldId == GoodOrderReportField.OrderPayTime.id){
				if(order.getPayTime() > 0){
					fieldValues[i] = TimeUtil.formatDate(order.getPayTime(), TimeFormat.yyyy_MM_dd_HH_mm);
				}
			}else if(fieldId == GoodOrderReportField.OrderCreditPointPrice.id){
				fieldValues[i] = moneyPrefix+order.getUseCreditPointPrice().toString();
			}else if(fieldId == GoodOrderReportField.OrderCreditPointGiven.id){
				fieldValues[i] = String.valueOf(order.getGetCreatePointCount());
			}else if(fieldId == GoodOrderReportField.UserAddress.id){
				fieldValues[i] = order.getUserProvince() + " " + order.getUserCity() + " " + order.getUserAddress();
			}else{
				final GoodOrderReportField field = GoodOrderReportField.parse(fieldId);
				if(field != null){
					Object value = SasUtil.readObjectProperty(order, field.propertyName, "");
					fieldValues[i] = String.valueOf(value);
				}
			}
		}
		return fieldValues;
	}
	
	
	/**************
	 * 创建活动成绩报表的副标题
	 * @return
	 */
	public static final List<String> createActivityScoreSubTitles(final SasMenuActivityScoreGroup group)
	{			
		final List<String> titles = new ArrayList<String>(2);
		titles.add("选手分组：" + ((group == null) ? "全部分组" : group.getName()));
		titles.add("导出日期：" + new SimpleDateFormat("yyyy-MM-dd HH: mm: ss").format(Calendar.getInstance().getTime()));
		return titles;
	}
	
	/****************
	 * 获取字段对应的title列表, 用户报表显示
	 */
	public static final String[] generateScoreFieldNames(final Long[] fieldIds)
	{	
		final List<String> result = new LinkedList<String>();
		if(ArrayUtils.isNotEmpty(fieldIds)){
			for(final Long fieldId: fieldIds)
			{
				final ScoreFieldProperty p = ScoreFieldProperty.parse(fieldId);
				if(p != null){
					result.add(p.title);
				}
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	/****************
	 * 按照字段id的顺序， 解析出字段的值， 每个数组代表一个选手的字段信息
	 * @param list
	 * @param fieldIds
	 * @return
	 */
	public static final List<String[]> generateAllActivityScoreFieldValues(final List<SimpleActivityScoreDTO> list, final Long[] fieldIds)
	{
		if(CollectionUtils.isEmpty(list) || ArrayUtils.isEmpty(fieldIds)){
			return new ArrayList<String[]>(0);
		}
		final List<String[]> result = new ArrayList<String[]>(list.size());
		for(final SimpleActivityScoreDTO score : list)
		{
			final List<String> fieldValues = new ArrayList<String>(fieldIds.length);
			for(final long fieldId : fieldIds)
			{
				if(ScoreFieldProperty.SexScoreRank.id == fieldId){//字段属性名字对不上号， 所以只能手动取
					if(score.getScoreSexRank() > 0){
						fieldValues.add(String.valueOf(score.getScoreSexRank()));
					}else{
						fieldValues.add("");
					}
				}else if(ScoreFieldProperty.ScoreRank.id == fieldId){
					if(score.getScoreRank() > 0){
						fieldValues.add(String.valueOf(score.getScoreRank()));
					}else{
						fieldValues.add("");
					}
				}else if(ScoreFieldProperty.SexScore2Rank.id == fieldId){//字段属性名字对不上号， 所以只能手动取
					if(score.getScoreSex2Rank() > 0){
						fieldValues.add(String.valueOf(score.getScoreSex2Rank()));
					}else{
						fieldValues.add("");
					}
				}else if(ScoreFieldProperty.Score2Rank.id == fieldId){
					if(score.getScore2Rank() > 0){
						fieldValues.add(String.valueOf(score.getScore2Rank()));
					}else{
						fieldValues.add("");
					}
				}else{
					final ScoreFieldProperty p = ScoreFieldProperty.parse(fieldId);
					if(p != null){
						Object value = SasUtil.readObjectProperty(score, p.property, "");
						fieldValues.add(String.valueOf(value));
					}
				}
			}
			result.add(fieldValues.toArray(new String[fieldValues.size()]) );
		}
		return result;
	}
	
	/*************
	 * 用户导出报表的副标题
	 * @param sas
	 * @param state
	 * @param userlevel
	 * @return
	 */
	public static final List<String> createUserReportSubTitles(final Sas sas, final UserState state,
			final UserLevel userlevel)
	{
		//生成状态名字列表
		final List<String> titles = new ArrayList<String>(3);
		titles.add("用户状态：" + (state == null ? "全部" : state.name));
		titles.add("用户身份：" + (userlevel == null ? "全部" : userlevel.name));
		titles.add("导出日期：" + new SimpleDateFormat("yyyy-MM-dd HH: mm: ss").format(Calendar.getInstance().getTime()));
		return titles;
	}
	
	/************
	 * 财务流水报告的二级标题
	 * @param sas
	 * @param types
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static final List<String> createSasFinanceReportSubTitles(final Sas sas, final FinanceType[] types,
			final long startTime, final long endTime)
	{
		//标题
		final StringBuilder title = new StringBuilder();
		for(final FinanceType t : types){
			if(title.length() > 0){
				title.append("，");
			}
			title.append(t.title);
		}
		//生成状态名字列表
		final List<String> titles = new ArrayList<String>(3);
		titles.add("财务类型：" + title.toString());
		titles.add("日期范围：" + TimeUtil.formatDate(startTime, TimeFormat.yyyy_MM_dd) + "至" + TimeUtil.formatDate(endTime, TimeFormat.yyyy_MM_dd));
		return titles;
	}
	
	/************
	 * 导出用户信息: new String[]{"昵称","真实姓名", "性别", "邮箱","手机","积分余额","身份","类型","状态","注册时间"},
	 * @param users
	 * @return
	 */
	public static final List<String[]> generateAllSasUserDetailsFieldValues(final List<SasUserReportDetail> users){
		if(CollectionUtils.isEmpty(users)){
			return new ArrayList<String[]>(0);
		}
		final List<String[]> result = new LinkedList<String[]>();
		for(final SasUserReportDetail user : users){
			result.add(new String[]{user.getNickname(), user.getTrueName(), user.getSex(), user.getEmail(),
					user.getPhone(), user.getCreditPointBalance(), 
					user.getActivityApplySuccessOrderCount()+"次",
					user.getGoodBuySuccessOrderCount()+"笔", user.getLevel(), user.getType(), 
					user.getState(), user.getCreateTime()});
		}		
		return result;
	}
}
