/**
 * 
 */
package com.sas.core.util;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.ActivityConstant.ActivityStyleClassSwitch;
import com.sas.core.constant.ActivityConstant.ApplierLimitType;
import com.sas.core.constant.CommonConstant.BinaryState;
import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.CommonConstant.TernaryState;
import com.sas.core.constant.EncryptConstant.Md5Salt;
import com.sas.core.constant.OrderConstant.OrderState;
import com.sas.core.constant.SasConstant.ApplySystemField;
import com.sas.core.constant.SasConstant.ChineseLanguageValue;
import com.sas.core.constant.SasConstant.FieldValueType;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.constant.TransactionConstant.OrderPayType;
import com.sas.core.constant.UserConstant.ApplierPersonType;
import com.sas.core.constant.UserConstant.EducationLevel;
import com.sas.core.constant.UserConstant.IdentityCardType;
import com.sas.core.constant.UserConstant.SexType;
import com.sas.core.dto.ActivityApplierLimitDTO;
import com.sas.core.dto.ActivityApplierVerifyResult;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.dto.FourEntry;
import com.sas.core.dto.SasApplyFieldDTO;
import com.sas.core.dto.SasFieldsWrapper;
import com.sas.core.dto.SasMenuActivityOrderApplierDetail;
import com.sas.core.dto.SasMenuActivityOrderApplierExcelDetail;
import com.sas.core.meta.SasMenuActivity;
import com.sas.core.meta.SasMenuActivityAdditionalService;
import com.sas.core.meta.SasMenuActivityAttachment;
import com.sas.core.meta.SasMenuActivityOrder;
import com.sas.core.meta.SasMenuActivityOrderApplier;
import com.sas.core.meta.SasMenuActivityOrderApplierWithPrivateFieldValueMap;
import com.sas.core.meta.SasMenuActivityStyleClass;
import com.sas.core.meta.SasPrivateApplyField;
import com.sas.core.service.SasMenuActivityOrderService;
import com.sas.core.util.meta.SasUtil;
import com.sas.core.util.meta.UserUtil;

/**
 * 报名人util
 * @author zhuliming
 *
 */
public class ActivityOrderApplierUtil { 
	
	protected static final Logger logger = Logger.getLogger(ActivityOrderApplierUtil.class);
	
	/**
	 * 自定义字段最大长度
	 */
	public static final int PrivateFieldMaxLength = 50;

	/**************
	 * 将字段id拼接成字符串在
	 * @param fieldIdsArray
	 * @return
	 */
	public static final String joinFieldIds(long[] fieldIdsArray)
	{
		final StringBuilder sb = new StringBuilder("");
		if(ArrayUtils.isNotEmpty(fieldIdsArray))
		{
			for(final long fieldId : fieldIdsArray){
				if(fieldId < 1){
					continue;
				}
				if(sb.length() > 0){
					sb.append(DividerChar.Comma.chars);
				}
				sb.append(fieldId);
			}
		}
		return sb.toString();
	}
	
	/*****************
	 * 将字段id转成对应的dto对象
	 * @param fieldIds
	 * @param fieldMap
	 * @return
	 */
	public static final SasFieldsWrapper<SasApplyFieldDTO> parseApplyFields(final String optionalFieldIdString,
			String mandatoryFieldIdString, final Map<Long, SasPrivateApplyField> privateFieldMap,
			final boolean onlyForApply, final boolean needEscape, final boolean isEnglishVersion,
			final boolean supportPersonTypeField, final boolean removeDeleteFieldFromRemainderFields)
	{
		//添加人物类型的字段
		if(supportPersonTypeField){
			if(StringUtils.isBlank(mandatoryFieldIdString)){
				mandatoryFieldIdString = String.valueOf(ApplySystemField.PersonType.id);
			}else{
				mandatoryFieldIdString = ApplySystemField.PersonType.id + DividerChar.Comma.chars + mandatoryFieldIdString;
			}
		}
		final String[] fieldIdStrings = new String[]{optionalFieldIdString, mandatoryFieldIdString};
		final List<Long[]> fieldIds = new ArrayList<Long[]>(fieldIdStrings.length);
		for(int i=0; i<fieldIdStrings.length; i++){
			if(StringUtils.isBlank(fieldIdStrings[i])){
				fieldIds.add(new Long[0]);
			}else{
				final String[] fieldIdArray = fieldIdStrings[i].split(DividerChar.Comma.chars);
				final List<Long> idArray = new LinkedList<Long>();
				for(final String fieldId : fieldIdArray){
					idArray.add(IdUtil.convertTolong(fieldId, 0L));
				}
				fieldIds.add(idArray.toArray(new Long[idArray.size()]));
			}
		}
		
		return ActivityOrderApplierUtil.parseApplyFields(fieldIds.get(0), fieldIds.get(1),
				privateFieldMap, onlyForApply, needEscape, supportPersonTypeField, isEnglishVersion,
				removeDeleteFieldFromRemainderFields);
	}
	
	public static final SasFieldsWrapper<SasApplyFieldDTO> parseApplyFields(final Long[] optionalFieldIds,
			final Long[] mandatoryFieldIds, final Map<Long, SasPrivateApplyField> privateFieldMap,
			final boolean onlyForApply, final boolean needEscape, final boolean supportPersonTypeField,
			final boolean isEnglishVersion, final boolean removeDeleteFieldFromRemainderFields)
	{
		final Set<Long> selectedFieldIds = new HashSet<Long>();
		final Long[][] fieldIdsPassed = new Long[][]{optionalFieldIds, mandatoryFieldIds};
		final List<SasApplyFieldDTO>[] selectedFields = new List[2];
		final List<Long> allFieldIds = new LinkedList<Long>();
		//parse field ids
		for(int i=0; i<fieldIdsPassed.length; i++){
			if(fieldIdsPassed[i] == null || fieldIdsPassed[i].length < 1){
				selectedFields[i] = new ArrayList<SasApplyFieldDTO>(0);
				continue;
			}
			final boolean isRequired = (i == 1); //是否是必填
			selectedFields[i] = new ArrayList<SasApplyFieldDTO>(fieldIdsPassed[i].length);
			for(final long id : fieldIdsPassed[i])
			{
				if(id < 1 || (onlyForApply && ApplySystemField.isNotApplyField(id))){
					continue;
				}
				selectedFieldIds.add(id);
				final ApplySystemField field = ApplySystemField.parse(id);
				if(field != null){//解析系统字段1-999
					selectedFields[i].add(new SasApplyFieldDTO(field, isRequired, isEnglishVersion));	
					allFieldIds.add(id);			
				}else{//解析自定义字段
					SasPrivateApplyField privateField = privateFieldMap.get(id);
					if(privateField != null){
						selectedFields[i].add(new SasApplyFieldDTO(privateField, needEscape, isRequired));
						allFieldIds.add(privateField.getId());
					}
				}
			}			
		}
		final SasFieldsWrapper<SasApplyFieldDTO> result = new SasFieldsWrapper<SasApplyFieldDTO>();
		result.setOptionalFields(selectedFields[0]);
		result.setMandatoryFields(selectedFields[1]);
		result.setAllFieldIds(allFieldIds);
		
		//计算剩余字段
		final List<SasApplyFieldDTO> remainderFields = new LinkedList<SasApplyFieldDTO>();
		for(final ApplySystemField field : ApplySystemField.allApplySystemFields()){
			if(!supportPersonTypeField && field == ApplySystemField.PersonType){
				continue;
			}
			if(!selectedFieldIds.contains(field.id)){
				remainderFields.add(new SasApplyFieldDTO(field, isEnglishVersion));
			}
		}
		for(Map.Entry<Long, SasPrivateApplyField> entry : privateFieldMap.entrySet()){
			final SasPrivateApplyField field = entry.getValue();
			if(!selectedFieldIds.contains(entry.getKey())
					&& (field.getDeleteState() == BinaryState.No.state || !removeDeleteFieldFromRemainderFields)){
				remainderFields.add(new SasApplyFieldDTO(entry.getValue(), needEscape));
			}
		}
		result.setRemainderFields(remainderFields);
		return result;		
	}
	
	/****************
	 * 是否包含了手机号的字段
	 * @param optionalFieldIds
	 * @param mandatoryFieldIds
	 * @return
	 */
	public static final boolean hasMobileField(String optionalFieldIds, String mandatoryFieldIds)
	{
		for(final String fieldIdString : new String[]{optionalFieldIds, mandatoryFieldIds})
		{
			if(StringUtils.isNotBlank(fieldIdString))
			{
				final String[] fieldIdArray = fieldIdString.split(DividerChar.Comma.chars);
				for(final String fieldId : fieldIdArray){
					if(IdUtil.convertTolong(fieldId, 0L) == ApplySystemField.Mobile.id){
						return true;
					}
				}
			}
		}
		return false;			
	}

	/******************
	 * 分析必填字段和可选字段的id列表， 已经去重了， 必填字段在前面
	 * @param mandatoryFieldIds
	 * @param optionalFieldIds
	 * @return
	 */
	public static final List<Long> parseFieldIdsBySort(final String mandatoryFieldIds, final String optionalFieldIds,
			final boolean onlyForApply, final boolean needUserTypeField)
	{
		final Set<Long> allFieldIdSet = new HashSet<Long>();
		final List<Long> result = new LinkedList<Long>();
		//添加用户类型：成人或者儿童
		if(needUserTypeField){
			allFieldIdSet.add(ApplySystemField.PersonType.id);
			result.add(ApplySystemField.PersonType.id);
		}
		for(final String fieldIdString : new String[]{mandatoryFieldIds, optionalFieldIds})
		{
			if(StringUtils.isNotBlank(fieldIdString))
			{
				final List<Long> idList = new LinkedList<Long>();
				final String[] fieldIdArray = fieldIdString.split(DividerChar.Comma.chars);
				for(final String fieldId : fieldIdArray){
					final long id = IdUtil.convertTolong(fieldId, 0L);
					if(id > 0 && !allFieldIdSet.contains(id) && !(onlyForApply && ApplySystemField.isNotApplyField(id))){
						allFieldIdSet.add(id);
						idList.add(id);
					}
				}
				if(idList.size() > 0){
					Collections.sort(idList);
					result.addAll(idList);
				}
			}
		}
		return result;	
	}
	
	/*******************
	 * 转成map， key为id，value=true表示必填， value=false表示可选
	 * @param mandatoryFieldIds
	 * @param optionalFieldIds
	 * @return
	 */
	public static final Map<Long, Boolean> parseSystemFieldRequiredProperties(final String mandatoryFieldIds, final String optionalFieldIds)
	{
		final Map<Long, Boolean> allFieldIdMap = new HashMap<Long, Boolean>();
		if(StringUtils.isNotBlank(mandatoryFieldIds))
		{
			final String[] fieldIdArray = mandatoryFieldIds.split(DividerChar.Comma.chars);
			for(final String fieldId : fieldIdArray){
				final long id = IdUtil.convertTolong(fieldId, 0L);
				if(ApplySystemField.isSystemField(id) && !ApplySystemField.isNotApplyField(id)){
					allFieldIdMap.put(id, Boolean.TRUE);
				}
			}
		}
		if(StringUtils.isNotBlank(optionalFieldIds))
		{
			final String[] fieldIdArray = optionalFieldIds.split(DividerChar.Comma.chars);
			for(final String fieldId : fieldIdArray){
				final long id = IdUtil.convertTolong(fieldId, 0L);
				if(ApplySystemField.isSystemField(id) && !ApplySystemField.isNotApplyField(id)){
					allFieldIdMap.put(id, Boolean.FALSE);
				}
			}
		}
		return allFieldIdMap;	
	}
	
	public static final Map<Long, Boolean> parseAllFieldRequiredProperties(final String mandatoryFieldIds, final String optionalFieldIds)
	{
		final Map<Long, Boolean> allFieldIdMap = new HashMap<Long, Boolean>();
		if(StringUtils.isNotBlank(mandatoryFieldIds))
		{
			final String[] fieldIdArray = mandatoryFieldIds.split(DividerChar.Comma.chars);
			for(final String fieldId : fieldIdArray){
				final long id = IdUtil.convertTolong(fieldId, 0L);
				if(!ApplySystemField.isNotApplyField(id)){
					allFieldIdMap.put(id, Boolean.TRUE);
				}
			}
		}
		if(StringUtils.isNotBlank(optionalFieldIds))
		{
			final String[] fieldIdArray = optionalFieldIds.split(DividerChar.Comma.chars);
			for(final String fieldId : fieldIdArray){
				final long id = IdUtil.convertTolong(fieldId, 0L);
				if(!ApplySystemField.isNotApplyField(id)){
					allFieldIdMap.put(id, Boolean.FALSE);
				}
			}
		}
		return allFieldIdMap;	
	}
	
	/**************************
	 * 解析出id数组
	 * @param mandatoryFieldIds
	 * @param optionalFieldIds
	 * @return
	 */
	public static final List<Long> parseFieldIds(final String mandatoryFieldIds, final String optionalFieldIds)
	{
		final List<Long> result = new LinkedList<Long>();
		if(StringUtils.isNotBlank(mandatoryFieldIds))
		{
			final String[] fieldIdArray = mandatoryFieldIds.split(DividerChar.Comma.chars);
			for(final String fieldId : fieldIdArray){
				final long id = IdUtil.convertTolong(fieldId, 0L);
				if(id > 0){
					result.add(id);
				}
			}
		}
		if(StringUtils.isNotBlank(optionalFieldIds))
		{
			final String[] fieldIdArray = optionalFieldIds.split(DividerChar.Comma.chars);
			for(final String fieldId : fieldIdArray){
				final long id = IdUtil.convertTolong(fieldId, 0L);
				if(id > 0){
					result.add(id);
				}
			}
		}
		return result;	
	}
	
	/****************
	 * 获取字段对应的title列表, 用户报表显示
	 */
	public static final String[] generateApplierFieldNames(final List<Long> fieldIdList, final Map<Long, SasPrivateApplyField> privateFieldMap, 
			final boolean needSplitContactPersonAndIdentityCard, final boolean needSplitProvinceAndCity, final boolean isEnglish)
	{	
		final List<String> result = new LinkedList<String>();
		for(final Long fieldId: fieldIdList)
		{
			final ApplySystemField systemField = ApplySystemField.parse(fieldId);
			if(systemField != null){
				if(fieldId == ApplySystemField.PersonType.id){
					if(isEnglish){
						result.add(ChineseLanguageValue.ApplierPersonType.englishName);
					}else{
						result.add(ChineseLanguageValue.ApplierPersonType.chineseName);
					}
				}else if(fieldId == ApplySystemField.EmergencyContactPerson.id && needSplitContactPersonAndIdentityCard){
					if(isEnglish){
						result.add(ChineseLanguageValue.EmergencyContactPersonName.englishName);
						result.add(ChineseLanguageValue.EmergencyContactPersonPhone.englishName);
					}else{
						result.add(ChineseLanguageValue.EmergencyContactPersonName.chineseName);
						result.add(ChineseLanguageValue.EmergencyContactPersonPhone.chineseName);
					}
				}else if(fieldId == ApplySystemField.IdentityInfo.id && needSplitContactPersonAndIdentityCard){
					if(isEnglish){
						result.add(ChineseLanguageValue.IdentityTypeName.englishName);
						result.add(ChineseLanguageValue.IdentityCodeName.englishName);						
					}else{
						result.add(ChineseLanguageValue.IdentityTypeName.chineseName);
						result.add(ChineseLanguageValue.IdentityCodeName.chineseName);
					}
				}else if(fieldId == ApplySystemField.HouseAddress.id){
					if(needSplitProvinceAndCity){
						if(isEnglish){
							result.add(ChineseLanguageValue.HouseAddressProvince.englishName);
							result.add(ChineseLanguageValue.HouseAddressCity.englishName);
							result.add(ChineseLanguageValue.HouseAddressDetail.englishName);
						}else{
							result.add(ChineseLanguageValue.HouseAddressProvince.chineseName);
							result.add(ChineseLanguageValue.HouseAddressCity.chineseName);
							result.add(ChineseLanguageValue.HouseAddressDetail.chineseName);
						}
					}else{
						result.add( isEnglish ? systemField.fieldEnglishName : systemField.fieldName);
					}
				}else if(fieldId == ApplySystemField.MailAddress.id){
					if(needSplitProvinceAndCity){
						if(isEnglish){
							result.add(ChineseLanguageValue.MailAddressProvince.englishName);
							result.add(ChineseLanguageValue.MailAddressCity.englishName);
							result.add(ChineseLanguageValue.MailAddressDetail.englishName);
						}else{
							result.add(ChineseLanguageValue.MailAddressProvince.chineseName);
							result.add(ChineseLanguageValue.MailAddressCity.chineseName);
							result.add(ChineseLanguageValue.MailAddressDetail.chineseName);
						}
					}else{
						result.add( isEnglish ? systemField.fieldEnglishName : systemField.fieldName);
					}
				}else{			
					result.add( isEnglish ? systemField.fieldEnglishName : systemField.fieldName);
				}				
			}else if(fieldId == ApplySystemField.OrderCouponDiscount.id){
				if(isEnglish){
					result.add(ChineseLanguageValue.CouponDiscout.englishName);
				}else{
					result.add(ChineseLanguageValue.CouponDiscout.chineseName);
				}
			}else{//自定义字段
				final SasPrivateApplyField field = privateFieldMap.get(fieldId);
				if(field != null){
					result.add(field.getName());
				}else{
					result.add(isEnglish ? "unknown field" : "未知字段");
				}
			}
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
	public static final List<String[]> generateAllApplierReportDetailsFieldValues(final List<SasMenuActivityOrderApplierDetail> appliers,
			final List<Long> fieldIdList,  final Map<Long, SasPrivateApplyField> privateFieldMap,
			final boolean needSplitContactPersonAndIdentityCard,
			final boolean needSplitProvinceAndCity, final boolean needHiddenPrivateInfo, final boolean isEnglish, final boolean needAddMoneySimbolPrefix)
	{
		if(CollectionUtils.isEmpty(appliers) || CollectionUtils.isEmpty(fieldIdList)){
			return new ArrayList<String[]>();
		}
		final List<String[]> result = new ArrayList<String[]>(appliers.size());
		for(final SasMenuActivityOrderApplierDetail detail : appliers)
		{
			if(needHiddenPrivateInfo){
				detail.setContactPhone(UserUtil.hiddenMobile(detail.getContactPhone()));
				detail.setContactTrueName(UserUtil.hiddenUserName(detail.getContactTrueName()));
				detail.setEmergencyContactPersonName(UserUtil.hiddenUserName(detail.getEmergencyContactPersonName()));
				detail.setEmergencyContactPersonPhone(UserUtil.hiddenMobile(detail.getEmergencyContactPersonPhone()));
				detail.setMobile(UserUtil.hiddenMobile(detail.getMobile()));
				detail.setTelephone(UserUtil.hiddenMobile(detail.getTelephone()));
				detail.setUserIdentityCode(UserUtil.hiddenIdentityCode(detail.getUserIdentityCode()));
				detail.setTrueName(UserUtil.hiddenUserName(detail.getTrueName()));
			}
			final String[] fieldValues =  ActivityOrderApplierUtil.generateApplierFieldValues(detail, fieldIdList, detail,
					needSplitContactPersonAndIdentityCard, needSplitProvinceAndCity, isEnglish, needAddMoneySimbolPrefix).key;
			if(ArrayUtils.isNotEmpty(fieldValues)){
				result.add(fieldValues);
			}
		}
		return result;
	}
  
  /**
   * 相同报名人订单信息只显示第一个报名人上
   * @return 返回值用于判断下一页第一条数据是否显示
   */
	public static String handleApplierSameOrder(final List<SasMenuActivityOrderApplierDetail> list,
                                                                         final String lastOrderCode,
                                                                         final Map<String, Integer> additionalServiceTitlesMap,
                                                                         final Map<String, BigDecimal> exportTotalOrderPrice,
                                                                         final boolean isEnglish){
	  if (null == list || list.size() < 1){
	    return null;
    }
    final String currentLastOrderCode = list.get(list.size() - 1).getOrderCode();
	  final Map<String, List<SasMenuActivityOrderApplierDetail>> applierMap = new HashMap<String, List<SasMenuActivityOrderApplierDetail>>();
	  for (int i = 0; i < list.size(); i++){
	    final SasMenuActivityOrderApplierDetail applierDetail = list.get(i);
	    if (StringUtils.isNotEmpty(lastOrderCode) && lastOrderCode.equals(applierDetail.getOrderCode())){
	        applierDetail.setOrderCode("");
          applierDetail.setUserRemark("");
          applierDetail.setDiscountedPrice(0);
          applierDetail.setGetCreatePointCount(0);
          applierDetail.setCaptainRemark("");
          if (applierDetail.getFixedAmount() == BinaryState.Yes.state){
            applierDetail.setPersonType("");
          }
          applierDetail.setTotalPriceDefault();
          applierDetail.setOrderNamePhone();
          applierDetail.setPayTypeDesc();
          applierDetail.setOrderStateDesc();
	      continue;
      }
	    List<SasMenuActivityOrderApplierDetail> appliers = applierMap.get(applierDetail.getOrderCode());
      if (appliers == null){
        appliers = new LinkedList<SasMenuActivityOrderApplierDetail>();
        applierMap.put(applierDetail.getOrderCode(), appliers);
      }
      appliers.add(applierDetail);
    }
    for (final Map.Entry<String, List<SasMenuActivityOrderApplierDetail>> orderCodeAppliers: applierMap.entrySet()){
      final List<SasMenuActivityOrderApplierDetail> appliers = orderCodeAppliers.getValue();
      final SasMenuActivityOrderApplierDetail firstApplier = appliers.get(0);
      firstApplier.setTotalPrice();
      firstApplier.setOrderStateDesc();
      firstApplier.setPayTypeDesc();
      firstApplier.setOrderNamePhone();
      firstApplier.setDepositePriceDesc();
      firstApplier.setEveryPersonUseCreditPointPriceDesc();
      if (firstApplier.getFixedAmount() == BinaryState.Yes.state){
        firstApplier.setPersonType(ApplierPersonType.FixedAmount.nameByLanguage(isEnglish));
      }
      BigDecimal totalPrice = exportTotalOrderPrice.get("totalPrice");
      if (totalPrice == null){
        exportTotalOrderPrice.put("totalPrice", firstApplier.getTotalPrice());
      }else {
        exportTotalOrderPrice.put("totalPrice", totalPrice.add(firstApplier.getTotalPrice()));
      }
      
      final String titles = firstApplier.getAdditionalServiceTitles();
      if (StringUtils.isNotEmpty(titles)){
        String[] titleArray = titles.split(DividerChar.AllCommas.chars);
        for (int i = 0; i < titleArray.length; i++){
          final Integer count = additionalServiceTitlesMap.get(titleArray[i]);
          if (null == count){
            additionalServiceTitlesMap.put(titleArray[i], firstApplier.getApplyCount());
          }else {
            additionalServiceTitlesMap.put(titleArray[i], count + firstApplier.getApplyCount());
          }
        }
      }
      
      if (appliers.size() > 1){
        for (int i = 1; i < appliers.size(); i++){
          final SasMenuActivityOrderApplierDetail applier = appliers.get(i);
          applier.setOrderCode("");
          applier.setUserRemark("");
          applier.setGetCreatePointCount(0);
          applier.setDiscountedPrice(0);
          applier.setCaptainRemark("");
          if (applier.getFixedAmount() == BinaryState.Yes.state){
            applier.setPersonType("");
          }
          applier.setTotalPrice();
          applier.setTotalPriceDefault();
          applier.setOrderNamePhone();
          applier.setPayTypeDesc();
          applier.setOrderStateDesc();
        }
      }
    }
	  return currentLastOrderCode;
  }
	
	/****************
	 * 按照字段id的顺序， 解析出字段的值， 每个数组代表一个报名人的字段信息
	 * @param appliers
	 * @param fieldIdList
	 * @param privateFieldMap
	 * @return
	 */
	public static final List<String[]> generateAllAppliersFieldValues(final List<SasMenuActivityOrderApplier> appliers,
			final List<Long> fieldIdList,  final Map<Long, SasPrivateApplyField> privateFieldMap, 
			final boolean needSplitContactPersonAndIdentityCard, final boolean needSplitProvinceAndCity, final boolean isEnglish)
	{
		final List<String[]> result = new ArrayList<String[]>(appliers.size());
		for(final SasMenuActivityOrderApplier applier : appliers)
		{
			final String[] fieldValues =  ActivityOrderApplierUtil.generateApplierFieldValues(applier, fieldIdList, 
					needSplitContactPersonAndIdentityCard, needSplitProvinceAndCity, isEnglish, true).key;
			if(ArrayUtils.isNotEmpty(fieldValues)){
				result.add(fieldValues);
			}
		}		
		return result;
	}

	public static final List<String[]> generateAllAppliersFieldValuesWithPrivateFieldValueMap(final List<SasMenuActivityOrderApplierWithPrivateFieldValueMap> appliers,
			final List<Long> fieldIdList,  final Map<Long, SasPrivateApplyField> privateFieldMap, 
			final boolean needSplitContactPersonAndIdentityCard, final boolean needSplitProvinceAndCity, final boolean isEnglish)
	{
		final List<String[]> result = new ArrayList<String[]>(appliers.size());
		for(final SasMenuActivityOrderApplierWithPrivateFieldValueMap applier : appliers)
		{
			final String[] fieldValues =  ActivityOrderApplierUtil.generateApplierFieldValues(applier, 
					fieldIdList, needSplitContactPersonAndIdentityCard, needSplitProvinceAndCity, isEnglish, true).key;
			if(ArrayUtils.isNotEmpty(fieldValues)){
				result.add(fieldValues);
			}
		}		
		return result;
	}
	
	/********************
	 * 将自定义字段的值解析成map
	 * @return
	 */
	public static final Map<String, Object> convertPrivateFiledValues2Map(final String privateFieldValues)
	{
		if(StringUtils.isNotBlank(privateFieldValues)){
			return JsonUtil.getObject(privateFieldValues, Map.class);
		}else{
			return new HashMap<String, Object>();
		}	
	}
	
	/******************
	 * 按照字段id的顺序， 将报名人的字段值解析到数组
	 * @param applier:支持SasMenuActivityOrderApplierDetail中的字段
	 * @param fieldIdList
	 * @param privateFieldMap
	 * @return
	 */
	public static final BinaryEntry<String[], Map<Long, String>> generateApplierFieldValues(final SasMenuActivityOrderApplier applier,
			final List<Long> fieldIdList,  final boolean needSplitContactPersonAndIdentityCard, 
			final boolean needSplitProvinceAndCity, final boolean isEnglish, final boolean needAddMoneySimbolPrefix)
	{
		final String moneyPrefix = needAddMoneySimbolPrefix ? "￥" : "";
		//生成结果
		final List<String> fieldValues = new ArrayList<String>(fieldIdList.size());
		final Map<Long, String> fieldValueMapById = new HashMap<Long, String>();		
		for(final long fieldId : fieldIdList)
		{
			if(fieldId < 1){
				continue;
			}
			if(fieldId == ApplySystemField.OrderTotalPrice.id || fieldId == ApplySystemField.OrderCreditPointPrice.id){
				final String fieldValue = ActivityOrderApplierUtil.generateApplierOneFieldValue(applier, fieldId, isEnglish);
				if(StringUtils.isBlank(fieldValue)){
					fieldValues.add("");
				}else{
					fieldValues.add(moneyPrefix + fieldValue);
				}		
				fieldValueMapById.put(fieldId, fieldValue);
			}else if(fieldId == ApplySystemField.OrderDepositePrice.id){
				if(applier instanceof SasMenuActivityOrderApplierDetail){	
					String fieldValue = null;
					if (!needAddMoneySimbolPrefix){
					  fieldValue = ((SasMenuActivityOrderApplierDetail)applier).getDepositePriceDesc();
          }else {
					  if(((SasMenuActivityOrderApplierDetail) applier).getOrderDepositType() == BinaryState.Yes.state){
						final int applyCount = ((SasMenuActivityOrderApplierDetail)applier).getApplyCount();
						final BigDecimal depositePrice = ((SasMenuActivityOrderApplierDetail)applier).getDepositePrice();
						final BigDecimal retainage = ((SasMenuActivityOrderApplierDetail)applier).getTotalPrice().subtract(depositePrice);
						fieldValue = moneyPrefix + ((SasMenuActivityOrderApplierDetail)applier).getEveryPersonDepositePrice()
								+ "(尾款"+ moneyPrefix + ((SasUtil.isZero(retainage) || applyCount < 1 ) ? "0" : retainage.divide(new BigDecimal(applyCount),2 , BigDecimal.ROUND_HALF_UP)) + ")";
            }else{
              fieldValue = "全款支付";
            }
          }
					fieldValues.add(fieldValue);
					fieldValueMapById.put(fieldId, fieldValue);
				}else{
					fieldValues.add("");
					fieldValueMapById.put(fieldId, "");
				}				
			}else if(fieldId == ApplySystemField.OrderContactPerson.id){
				if(applier instanceof SasMenuActivityOrderApplierDetail){
					final String orderNamePhone = ((SasMenuActivityOrderApplierDetail)applier).getOrderNamePhone();
					fieldValues.add(orderNamePhone);
					fieldValueMapById.put(fieldId, DividerChar.mergeStrings(new String[]{orderNamePhone}, DividerChar.ActivityFieldValue.chars));
				}else{
					fieldValues.add("");
					fieldValueMapById.put(fieldId, DividerChar.ActivityFieldValue.chars);
				}				
			}else if(fieldId == ApplySystemField.OrderState.id){
				final String fieldValue = ActivityOrderApplierUtil.generateApplierOneFieldValue(applier, fieldId, isEnglish);
				fieldValues.add(fieldValue);
				if(applier instanceof SasMenuActivityOrderApplierDetail){
					fieldValueMapById.put(fieldId, String.valueOf(((SasMenuActivityOrderApplierDetail)applier).getState()));
				}else{
					fieldValueMapById.put(fieldId, "");
				}
			}else if(fieldId == ApplySystemField.EmergencyContactPerson.id){
				final String emergencyContactPersonName = StringUtils.isNotBlank(applier.getEmergencyContactPersonName()) ? applier.getEmergencyContactPersonName() : "";
				final String emergencyContactPersonPhone = StringUtils.isNotBlank(applier.getEmergencyContactPersonPhone()) ? applier.getEmergencyContactPersonPhone() : "";
				if(needSplitContactPersonAndIdentityCard){
					fieldValues.add(emergencyContactPersonName);
					fieldValues.add(emergencyContactPersonPhone);
				}else{
					fieldValues.add(emergencyContactPersonName + " " + emergencyContactPersonPhone);
				}
				fieldValueMapById.put(fieldId, DividerChar.mergeStrings(new String[]{emergencyContactPersonName, emergencyContactPersonPhone}, DividerChar.ActivityFieldValue.chars));
			}else if(fieldId == ApplySystemField.IdentityInfo.id){
				final IdentityCardType identityCardType = IdentityCardType.parse(applier.getUserIdentityCardType());
				if(needSplitContactPersonAndIdentityCard){
					fieldValues.add( isEnglish ? identityCardType.englishName : identityCardType.name );
					fieldValues.add(applier.getUserIdentityCode());
				}else{
					fieldValues.add((isEnglish ? identityCardType.englishName : identityCardType.name) + " " 
							+ applier.getUserIdentityCode());
				}
				fieldValueMapById.put(fieldId, DividerChar.mergeStrings(new String[]{String.valueOf(identityCardType.type),
						applier.getUserIdentityCode()}, DividerChar.ActivityFieldValue.chars));
			}else if(fieldId == ApplySystemField.Sex.id){
				final SexType sextype = SexType.parse(applier.getSex());
				fieldValues.add(isEnglish ? sextype.englishName : sextype.name);
				fieldValueMapById.put(fieldId, String.valueOf(sextype.type));
			}else if(fieldId == ApplySystemField.MailAddress.id){
				if(needSplitProvinceAndCity){
					fieldValues.add(applier.getMailProvince());
					fieldValues.add(applier.getMailCity());
					fieldValues.add(applier.getMailAddress());
				}else{
					fieldValues.add(applier.getMailProvince() + applier.getMailCity() + applier.getMailAddress());
				}
				fieldValueMapById.put(fieldId, applier.getMailProvince() + DividerChar.ActivityFieldValue.chars + applier.getMailCity() 
					+ DividerChar.ActivityFieldValue.chars + applier.getMailAddress());
			}else if(fieldId == ApplySystemField.HouseAddress.id){
				if(needSplitProvinceAndCity){
					fieldValues.add(applier.getHouseProvince());
					fieldValues.add(applier.getHouseCity());
					fieldValues.add(applier.getHouseAddress());
				}else{
					fieldValues.add(applier.getHouseProvince() + applier.getHouseCity() + applier.getHouseAddress());
				}
				fieldValueMapById.put(fieldId, applier.getHouseProvince() + DividerChar.ActivityFieldValue.chars + applier.getHouseCity()
					+ DividerChar.ActivityFieldValue.chars + applier.getHouseAddress());
			}else{
				final String fieldValue = ActivityOrderApplierUtil.generateApplierOneFieldValue(applier, fieldId, isEnglish);
				fieldValues.add(fieldValue);
				fieldValueMapById.put(fieldId, fieldValue);
			}
		}
		return new BinaryEntry<String[], Map<Long, String>>(fieldValues.toArray(new String[fieldValues.size()]),
				fieldValueMapById);
	}
	/******************
	 * 按照字段id的顺序， 将报名人的字段值解析到数组, 添加了优惠券字段
	 * @param applier:支持SasMenuActivityOrderApplierDetail中的字段
	 * @param fieldIdList
	 * @param privateFieldMap
	 * @return
	 */
	public static final BinaryEntry<String[], Map<Long, String>> generateApplierFieldValues(final SasMenuActivityOrderApplier applier,
			final List<Long> fieldIdList,final SasMenuActivityOrderApplierDetail detail,  final boolean needSplitContactPersonAndIdentityCard,
			final boolean needSplitProvinceAndCity, final boolean isEnglish, final boolean needAddMoneySimbolPrefix)
	{
		final String moneyPrefix = needAddMoneySimbolPrefix ? "￥" : "";
		//生成结果
		final List<String> fieldValues = new ArrayList<String>(fieldIdList.size());
		final Map<Long, String> fieldValueMapById = new HashMap<Long, String>();
		for(final long fieldId : fieldIdList)
		{
			if(fieldId < 1){
				continue;
			}
			if(fieldId == ApplySystemField.OrderTotalPrice.id || fieldId == ApplySystemField.OrderCreditPointPrice.id){
				final String fieldValue = ActivityOrderApplierUtil.generateApplierOneFieldValue(applier, fieldId, isEnglish);
				if(StringUtils.isBlank(fieldValue)){
					fieldValues.add("");
				}else{
					fieldValues.add(moneyPrefix + fieldValue);
				}
				fieldValueMapById.put(fieldId, fieldValue);
			}else if(fieldId == ApplySystemField.OrderDepositePrice.id){
				if(applier instanceof SasMenuActivityOrderApplierDetail){
					String fieldValue = null;
					if (!needAddMoneySimbolPrefix){
					  fieldValue = ((SasMenuActivityOrderApplierDetail)applier).getDepositePriceDesc();
          }else {
					  if(((SasMenuActivityOrderApplierDetail) applier).getOrderDepositType() == BinaryState.Yes.state){
						final int applyCount = ((SasMenuActivityOrderApplierDetail)applier).getApplyCount();
						final BigDecimal depositePrice = ((SasMenuActivityOrderApplierDetail)applier).getDepositePrice();
						final BigDecimal retainage = ((SasMenuActivityOrderApplierDetail)applier).getTotalPrice().subtract(depositePrice);
						fieldValue = moneyPrefix + ((SasMenuActivityOrderApplierDetail)applier).getEveryPersonDepositePrice()
								+ "(尾款"+ moneyPrefix + ((SasUtil.isZero(retainage) || applyCount < 1 ) ? "0" : retainage.divide(new BigDecimal(applyCount),2 , BigDecimal.ROUND_HALF_UP)) + ")";
            }else{
              fieldValue = "全款支付";
            }
          }
					fieldValues.add(fieldValue);
					fieldValueMapById.put(fieldId, fieldValue);
				}else{
					fieldValues.add("");
					fieldValueMapById.put(fieldId, "");
				}
			}else if(fieldId == ApplySystemField.OrderContactPerson.id){
				if(applier instanceof SasMenuActivityOrderApplierDetail){
					final String orderNamePhone = ((SasMenuActivityOrderApplierDetail)applier).getOrderNamePhone();
					fieldValues.add(orderNamePhone);
					fieldValueMapById.put(fieldId, DividerChar.mergeStrings(new String[]{orderNamePhone}, DividerChar.ActivityFieldValue.chars));
				}else{
					fieldValues.add("");
					fieldValueMapById.put(fieldId, DividerChar.ActivityFieldValue.chars);
				}
			}else if(fieldId == ApplySystemField.OrderState.id){
				final String fieldValue = ActivityOrderApplierUtil.generateApplierOneFieldValue(applier, fieldId, isEnglish);
				fieldValues.add(fieldValue);
				if(applier instanceof SasMenuActivityOrderApplierDetail){
					fieldValueMapById.put(fieldId, String.valueOf(((SasMenuActivityOrderApplierDetail)applier).getState()));
				}else{
					fieldValueMapById.put(fieldId, "");
				}
			}else if(fieldId == ApplySystemField.EmergencyContactPerson.id){
				final String emergencyContactPersonName = StringUtils.isNotBlank(applier.getEmergencyContactPersonName()) ? applier.getEmergencyContactPersonName() : "";
				final String emergencyContactPersonPhone = StringUtils.isNotBlank(applier.getEmergencyContactPersonPhone()) ? applier.getEmergencyContactPersonPhone() : "";
				if(needSplitContactPersonAndIdentityCard){
					fieldValues.add(emergencyContactPersonName);
					fieldValues.add(emergencyContactPersonPhone);
				}else{
					fieldValues.add(emergencyContactPersonName + " " + emergencyContactPersonPhone);
				}
				fieldValueMapById.put(fieldId, DividerChar.mergeStrings(new String[]{emergencyContactPersonName, emergencyContactPersonPhone}, DividerChar.ActivityFieldValue.chars));
			}else if(fieldId == ApplySystemField.IdentityInfo.id){
				final IdentityCardType identityCardType = IdentityCardType.parse(applier.getUserIdentityCardType());
				if(needSplitContactPersonAndIdentityCard){
					fieldValues.add( isEnglish ? identityCardType.englishName : identityCardType.name );
					fieldValues.add(applier.getUserIdentityCode());
				}else{
					fieldValues.add((isEnglish ? identityCardType.englishName : identityCardType.name) + " "
							+ applier.getUserIdentityCode());
				}
				fieldValueMapById.put(fieldId, DividerChar.mergeStrings(new String[]{String.valueOf(identityCardType.type),
						applier.getUserIdentityCode()}, DividerChar.ActivityFieldValue.chars));
			}else if(fieldId == ApplySystemField.Sex.id){
				final SexType sextype = SexType.parse(applier.getSex());
				fieldValues.add(isEnglish ? sextype.englishName : sextype.name);
				fieldValueMapById.put(fieldId, String.valueOf(sextype.type));
			}else if(fieldId == ApplySystemField.MailAddress.id){
				if(needSplitProvinceAndCity){
					fieldValues.add(applier.getMailProvince());
					fieldValues.add(applier.getMailCity());
					fieldValues.add(applier.getMailAddress());
				}else{
					fieldValues.add(applier.getMailProvince() + applier.getMailCity() + applier.getMailAddress());
				}
				fieldValueMapById.put(fieldId, applier.getMailProvince() + DividerChar.ActivityFieldValue.chars + applier.getMailCity()
					+ DividerChar.ActivityFieldValue.chars + applier.getMailAddress());
			}else if(fieldId == ApplySystemField.HouseAddress.id){
				if(needSplitProvinceAndCity){
					fieldValues.add(applier.getHouseProvince());
					fieldValues.add(applier.getHouseCity());
					fieldValues.add(applier.getHouseAddress());
				}else{
					fieldValues.add(applier.getHouseProvince() + applier.getHouseCity() + applier.getHouseAddress());
				}
				fieldValueMapById.put(fieldId, applier.getHouseProvince() + DividerChar.ActivityFieldValue.chars + applier.getHouseCity()
					+ DividerChar.ActivityFieldValue.chars + applier.getHouseAddress());
			}else if(fieldId == ApplySystemField.OrderCouponDiscount.id){
				if(0 != detail.getDiscountedPrice()){
					fieldValues.add("减￥" + detail.getDiscountedPrice());
				}else{
					fieldValues.add(" ");
				}
			}else{
				final String fieldValue = ActivityOrderApplierUtil.generateApplierOneFieldValue(applier, fieldId, isEnglish);
				fieldValues.add(fieldValue);
				fieldValueMapById.put(fieldId, fieldValue);
			}
		}
		return new BinaryEntry<String[], Map<Long, String>>(fieldValues.toArray(new String[fieldValues.size()]),
				fieldValueMapById);
	}
	public static final String generateApplierOneFieldValue(final SasMenuActivityOrderApplier applier, 
			final Long fieldId,  final boolean isEnglish)
	{
		//将自定义字段的值解析成map
		final Map<String, Object> privateFieldValuesMap = ActivityOrderApplierUtil.convertPrivateFiledValues2Map(applier.getPrivateFiledValues());
		if(fieldId == ApplySystemField.PersonType.id){
		  if (StringUtils.isBlank(applier.getPersonType())){
		    return "";
      }else {
		    return ApplierPersonType.parse(applier.getPersonType()).nameByLanguage(isEnglish);
      }
			
		}else if(fieldId == ApplySystemField.OrderCreateTime.id){
			if(applier instanceof SasMenuActivityOrderApplierDetail){	
				final Long time = ((SasMenuActivityOrderApplierDetail)applier).getCreateTime();
				return (time > 0) ? TimeUtil.formatDate(time, TimeFormat.MM_dd_HH_mm_ss) : "";
			}
			return "";
		}else if(fieldId == ApplySystemField.OrderPayType.id){
			return (applier instanceof SasMenuActivityOrderApplierDetail) 
					? ((SasMenuActivityOrderApplierDetail)applier).getPayTypeDesc() : "";
		}else if(fieldId == ApplySystemField.OrderTotalPrice.id){
			if(applier instanceof SasMenuActivityOrderApplierDetail){
			  final int totalPrice = ((SasMenuActivityOrderApplierDetail)applier).getTotalPrice().intValue();
				return totalPrice == -1 ? "" : String.valueOf(totalPrice);
			}
			return "";			
		}else if(fieldId == ApplySystemField.OrderDepositePrice.id){
			if(applier instanceof SasMenuActivityOrderApplierDetail){	
				if(((SasMenuActivityOrderApplierDetail) applier).getOrderDepositType() == BinaryState.Yes.state){
					final int applyCount = ((SasMenuActivityOrderApplierDetail)applier).getApplyCount();
					final BigDecimal depositePrice = ((SasMenuActivityOrderApplierDetail)applier).getDepositePrice();
					final BigDecimal retainage = ((SasMenuActivityOrderApplierDetail)applier).getTotalPrice().subtract(depositePrice);
					return ((SasMenuActivityOrderApplierDetail)applier).getEveryPersonDepositePrice()
							+ "(尾款"+ ((SasUtil.isZero(retainage) || applyCount < 1 ) ? "0" : retainage.divide(new BigDecimal(applyCount),2 , BigDecimal.ROUND_HALF_UP)) + ")";
				}else{
					return "全款支付";
				}
			}
			return "";			
		}else if(fieldId == ApplySystemField.OrderCreditPointPrice.id){
			if(applier instanceof SasMenuActivityOrderApplierDetail){					
				return ((SasMenuActivityOrderApplierDetail)applier).getEveryPersonUseCreditPointPriceDesc();
			}
			return "";				
		}else if(fieldId == ApplySystemField.OrderContactPerson.id){
			if(applier instanceof SasMenuActivityOrderApplierDetail){
			  final String contactTrueName = ((SasMenuActivityOrderApplierDetail)applier).getContactTrueName();
			  final String contactPhone = ((SasMenuActivityOrderApplierDetail)applier).getContactPhone();
				return contactTrueName + " " + contactPhone;
			}
			return "";			
		}else if(fieldId == ApplySystemField.OrderState.id){
			if(applier instanceof SasMenuActivityOrderApplierDetail){
				final SasMenuActivityOrderApplierDetail detail = ((SasMenuActivityOrderApplierDetail)applier);
        return detail.getOrderStateDesc();
			}
			return "";
		}else if(fieldId == ApplySystemField.OrderSignTime.id){
			if(((SasMenuActivityOrderApplierDetail)applier).getLastSignTime() > 0){
			  final long signTime = ((SasMenuActivityOrderApplierDetail)applier).getLastSignTime();
				return signTime > 0 ? TimeUtil.formatDate(signTime,
						TimeFormat.MM_dd_HH_mm_ss) : "";
			}
			return "";
		}else if(fieldId == ApplySystemField.OrderPayTime.id){
			if(((SasMenuActivityOrderApplierDetail)applier).getPayTime() > 0){
				return TimeUtil.formatDate(((SasMenuActivityOrderApplierDetail)applier).getPayTime(),
						TimeFormat.MM_dd_HH_mm_ss);
			}
			return "";
		}else if(fieldId == ApplySystemField.Birthday.id){
			if(applier.getBirthdayYear() > 0){
				int month = applier.getBirthdayMonth();
				if(month > 11){
					month = 11;
				}
				int birthday = applier.getBirthdayday();
				if(birthday < 1){
					birthday = 1;
				}
				final StringBuilder dateString = new StringBuilder("");
				dateString.append(applier.getBirthdayYear() + "-");
				if(month < 9){
					dateString.append("0" + (month+1) + "-");
				}else{
					dateString.append((month+1) + "-");
				}
				if(birthday < 10){
					dateString.append("0" + birthday);
				}else{
					dateString.append(birthday);
				}
				return dateString.toString();
			}
			return "";
		}else if(fieldId == ApplySystemField.Age.id){
			if(applier.getAge() > 0){
				return String.valueOf(applier.getAge());
			}
			return "";
		}else if(fieldId == ApplySystemField.EmergencyContactPerson.id){
			final String emergencyContactPersonName = StringUtils.isNotBlank(applier.getEmergencyContactPersonName()) ? applier.getEmergencyContactPersonName() : "";
			final String emergencyContactPersonPhone = StringUtils.isNotBlank(applier.getEmergencyContactPersonPhone()) ? applier.getEmergencyContactPersonPhone() : "";
			return emergencyContactPersonName + " " + emergencyContactPersonPhone;
		}else if(fieldId == ApplySystemField.IdentityInfo.id){
			final IdentityCardType identityCardType = IdentityCardType.parse(applier.getUserIdentityCardType());
			return isEnglish ? identityCardType.englishName : identityCardType.name + " " 
						+ applier.getUserIdentityCode();
		}else if(fieldId == ApplySystemField.Education.id){
			final String educationLevelName = isEnglish ? EducationLevel.parse(applier.getEducationLevel()).englishName 
					: EducationLevel.parse(applier.getEducationLevel()).name;
			return educationLevelName.equalsIgnoreCase(applier.getEducationLevel()) ? educationLevelName : applier.getEducationLevel();
		}else if(fieldId == ApplySystemField.Sex.id){
			final SexType sextype = SexType.parse(applier.getSex());
			return isEnglish ? sextype.englishName : sextype.name;
		}else if( ! ApplySystemField.isSystemField(fieldId)){
			final Object v = privateFieldValuesMap.get(String.valueOf(fieldId));
			return (v == null) ? "" : String.valueOf(v);
		}else if(fieldId == ApplySystemField.MailAddress.id){
			return applier.getMailProvince() + applier.getMailCity() + applier.getMailAddress();
		}else if(fieldId == ApplySystemField.HouseAddress.id){
			return applier.getHouseProvince() + applier.getHouseCity() + applier.getHouseAddress();
		}else if (fieldId == ApplySystemField.OrderCreditPointGiven.id){
		  int pointCount = ((SasMenuActivityOrderApplierDetail)applier).getGetCreatePointCount();
		  return  pointCount > 0 ? String.valueOf(pointCount) : "";
    }else if(fieldId == ApplySystemField.BloodType.id){
			return "其他".equals(applier.getBloodType()) ? "其它"  : applier.getBloodType();
		}else{
			final ApplySystemField field = ApplySystemField.parse(fieldId);
			String fieldValue = "";
			if(field != null){
				final Object value = SasUtil.readObjectProperty(applier, field.propertyName, "");
				if(value != null){
					fieldValue = (value instanceof BigDecimal) ? ((BigDecimal)value).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
							: String.valueOf(value);
				}
			}
			return fieldValue;
		}
	}
	
	public static final String generateOrderOneFieldValue(final SasMenuActivityOrder order, final long fieldId)
	{
		if(fieldId == ApplySystemField.OrderCreateTime.id){
			return TimeUtil.formatDate(order.getCreateTime(), TimeFormat.MM_dd_HH_mm_ss);
		}else if(fieldId == ApplySystemField.OrderPayType.id){
			return OrderPayType.getTitle(order.getOrderPayType());
		}else if(fieldId == ApplySystemField.OrderTotalPrice.id){
			return order.getTotalPrice().toString();
		}else if(fieldId == ApplySystemField.OrderDepositePrice.id){
			if(order.getOrderDepositType() == BinaryState.Yes.state){
				return order.getDepositePrice().toString();
			}else{
				return "";
			}				
		}else if(fieldId == ApplySystemField.OrderCreditPointPrice.id){
			return order.getUseCreditPointPrice().toString();			
		}else if(fieldId == ApplySystemField.OrderContactPerson.id){
			return order.getContactTrueName() + " "+ order.getContactPhone();
		}else if(fieldId == ApplySystemField.OrderSignTime.id){
			if(order.getLastSignTime() > 0){
				return TimeUtil.formatDate(order.getLastSignTime(), 
						TimeFormat.MM_dd_HH_mm_ss);
			}else{
				return "";
			}
		}else if(fieldId == ApplySystemField.OrderPayTime.id){
			if(order.getPayTime() > 0){
				return TimeUtil.formatDate(order.getPayTime(), 
						TimeFormat.MM_dd_HH_mm_ss);
			}else{
				return "";
			}
		}else if(fieldId == ApplySystemField.OrderCaptainRemark.id){
			return order.getCaptainRemark();
		}else if(fieldId == ApplySystemField.OrderTeamName.id){
			return order.getTeamApplierGroupName();
		}else if(fieldId == ApplySystemField.OrderAdditionalService.id){
			return order.getAdditionalServiceTitles();	
		}else if(fieldId == ApplySystemField.OrderUserRemark.id){
			return order.getUserRemark();	
		}else if(fieldId == ApplySystemField.Nickname.id){
			return order.getUserNickname();
		}
		return "";	
	}
	
	/******************
	 * 参数代表一个报名人，用&分隔， 解析成对象
	 * 对于系统字段， key就是对应applier属性的字段名字
	 * 对于自定义字段， key就是对应的自定义字段id
	 * @param fieldMap
	 * @return
	 */
	public static final SasMenuActivityOrderApplier parseApplierFieldValues(final String fieldValues)
	{
		if(StringUtils.isBlank(fieldValues)){
			return null;
		}
		final List<BinaryEntry<String, String>> fields = new LinkedList<BinaryEntry<String, String>>();
		final String[] fieldNameValues = fieldValues.split(DividerChar.Amp.chars);		
		for(final String fieldNameValue: fieldNameValues)
		{
			final int dividerCharIndex = fieldNameValue.indexOf(DividerChar.Equal.chars);
			if(dividerCharIndex < 1){
				continue;
			}
			final String fieldName = fieldNameValue.substring(0, dividerCharIndex);
			final String fieldValue = (dividerCharIndex == (fieldNameValue.length()-1)) ? ""
					: HtmlUtil.decodeParam(fieldNameValue.substring(dividerCharIndex+1), Encoding.UTF8, "");
			if(StringUtils.isBlank(fieldName)){
				continue;
			}
			fields.add(new BinaryEntry<String, String>(fieldName, fieldValue));
		}
		return ActivityOrderApplierUtil.parseApplierFieldValues(fields);
	}
	
	
	/******************
	 * 参数代表一个报名人
	 * 对于系统字段， key就是对应applier属性的字段名字
	 * 对于自定义字段， key就是对应的自定义字段id
	 * @param fieldMap
	 * @return
	 */
	private static final SasMenuActivityOrderApplier parseApplierFieldValues(final List<BinaryEntry<String, String>> fieldNameValues)
	{
		if(CollectionUtils.isEmpty(fieldNameValues)){
			return null;
		}
		final SasMenuActivityOrderApplier applier = new SasMenuActivityOrderApplier();
		final Map<String, String> privateFieldMap = new HashMap<String, String>();		
		for(final BinaryEntry<String, String> fieldNameValue: fieldNameValues)
		{
			final String fieldName = fieldNameValue.key;
			final String fieldValue = HtmlUtil.filterChineseDigitalsOrLetters(fieldNameValue.value).replaceAll("　", " ").trim();
			if(StringUtils.isNumeric(fieldName)){//自定义字段
				final String oldFieldValue = privateFieldMap.get(fieldName);
				if(StringUtils.isNotBlank(oldFieldValue)){//多选
					privateFieldMap.put(fieldName, oldFieldValue + "，" + fieldValue);
				}else{//其他类型
					privateFieldMap.put(fieldName, fieldValue);
				}
			}else{
				if(ApplySystemField.PersonType.propertyName.equalsIgnoreCase(fieldName)){
					applier.setPersonType(fieldValue);
				}else if(ApplySystemField.OrderItemId.propertyName.equalsIgnoreCase(fieldName)){
					applier.setId(IdUtil.convertTolong(fieldValue, 0L));
				}else if(ApplySystemField.Sex.propertyName.equals(fieldName)){//char字段
					applier.setSex(SexType.parse(fieldValue).type);
				}else if(ChineseLanguageValue.IdentityTypeName.propertyName.equals(fieldName)){//char字段
					applier.setUserIdentityCardType(IdentityCardType.parse(fieldValue).type);
				}else if(ChineseLanguageValue.HouseAddressProvince.propertyName.equals(fieldName)){//居住地省
					applier.setHouseProvince(HtmlUtil.defaultIfEqual(fieldValue, "-请选择-", "").replaceAll("(省|市)", ""));
				}else if(ChineseLanguageValue.HouseAddressCity.propertyName.equals(fieldName)){//居住地市
					applier.setHouseCity(HtmlUtil.defaultIfEqual(fieldValue, "-请选择-", "").replace("市", ""));
				}else if(ChineseLanguageValue.MailAddressProvince.propertyName.equals(fieldName)){//通信地址省
					applier.setMailProvince(HtmlUtil.defaultIfEqual(fieldValue, "-请选择-", "").replaceAll("(省|市)", ""));
				}else if(ChineseLanguageValue.MailAddressCity.propertyName.equals(fieldName)){//通信地址市
					applier.setMailCity(HtmlUtil.defaultIfEqual(fieldValue, "-请选择-", "").replace("市", ""));
				}else if(ChineseLanguageValue.EmergencyContactPersonName.propertyName.equals(fieldName)){//紧急联系人
					applier.setEmergencyContactPersonName(fieldValue == null ? "" : fieldValue.trim());
				}else if(ChineseLanguageValue.EmergencyContactPersonPhone.propertyName.equals(fieldName)){//紧急联系人
					applier.setEmergencyContactPersonPhone(fieldValue == null ? "" : ValidatorUtil.parseValidPhoneLetters(HtmlUtil.removeMobileUTF8MB44AppliyField(fieldValue.trim())));
				}
				else{//数值型字段或者字符串型字段
					final ApplySystemField systemField = ApplySystemField.valueByPropertyName(fieldName);
					if(systemField != null) {
						if(systemField == ApplySystemField.Birthday)
						{
							final String newDateStr = TimeUtil.formatAndCheckDateAsFormatYYYY_MM_dd(fieldValue);
							if(newDateStr != null){					
								final long time = TimeUtil.parseDate2Miliseconds(newDateStr, TimeFormat.yyyy_MM_dd, 0L);
								final Calendar cal = Calendar.getInstance();
								cal.setTimeInMillis(time);
								applier.setBirthdayYear(cal.get(Calendar.YEAR));
								applier.setBirthdayMonth(cal.get(Calendar.MONTH));
								applier.setBirthdayday(cal.get(Calendar.DAY_OF_MONTH));
							}
						}else if(systemField.fieldType == FieldValueType.Digital){//数值型
						    SasUtil.setObjectProperty(applier, fieldName, IdUtil.convertToNumber(fieldValue, 0));
						}else{//字符串型
							SasUtil.setObjectProperty(applier, fieldName, HtmlUtil.removeMobileUTF8MB44AppliyField(fieldValue));
						}
					}
				}
			}
		}
		applier.setPrivateFiledValues(JsonUtil.getJsonString(privateFieldMap));
		if(applier.getUserIdentityCode() != null){
			applier.setUserIdentityCode(applier.getUserIdentityCode().replaceAll("x|ｘ|Ｘ", "X"));
		}
		return applier;
	}
	
	/*********************
	 * 将报名人的字段名称 和 每个人的各个字段的值 组装到一起
	 * @param names
	 * @param values
	 * @return
	 */
	public static final List<List<BinaryEntry<String, String>>> createApplierFieldNameValueEntries(final String[] names,
			final List<String[]> values)
	{
		final List<List<BinaryEntry<String, String>>> result = new ArrayList<List<BinaryEntry<String, String>>>(values.size());
		for(final String[] valueArray : values)
		{
			final int minLength = Math.min(names.length, valueArray.length);
			final List<BinaryEntry<String, String>> oneApplierFields = new ArrayList<BinaryEntry<String, String>>(minLength);
			for(int i=0; i<minLength; i++) {
				oneApplierFields.add(new BinaryEntry<String, String>(names[i], valueArray[i].replaceAll(DividerChar.ActivityFieldValue.chars, " ")));
			}
			result.add(oneApplierFields);
		}
		return result;
	}

	/**
	 * 将String数据字段进行XSS过滤处理
	 * @param strArray
	 * @return
	 */
	public static String[] filterStringArray(String[] strArray){
		for(int index = 0; index < strArray.length; index ++){
			strArray[index] = XSSUtil.filter(strArray[index], false);
		}
		return strArray;
	}
	
	/**
	 * 判断自定义字段名称长度
	 * @param name
	 * @return
	 */
	public static final boolean isPrivateFieldNameValid(final String name){
		if(StringUtils.isBlank(name) || name.length() > ActivityOrderApplierUtil.PrivateFieldMaxLength){
			return false;
		}
		return true;
	}
	
	/*********************
	 * 把需要更新的字段的值做下更新
	 * @param applierFromDatabase
	 * @param applierUpdate
	 * @param updateFields： key为字段id， value=true表示必填， value=false表示可选
	 */
	public static final SasMenuActivityOrderApplier updateApplierBasic(final SasMenuActivityOrderApplier applierFromDatabase, final SasMenuActivityOrderApplier applierUpdate,
			final Map<Long, Boolean> updateFields)
	{	
		for(final long systemFieldId: updateFields.keySet())
		{
			final ApplySystemField field = ApplySystemField.parse(systemFieldId);
			if(field == null || ApplySystemField.isNotApplyField(systemFieldId)){
				continue;
			}
			if(ApplySystemField.Birthday.id == systemFieldId){//生日
				applierFromDatabase.setBirthdayYear(applierUpdate.getBirthdayYear());
				applierFromDatabase.setBirthdayMonth(applierUpdate.getBirthdayMonth());
				applierFromDatabase.setBirthdayday(applierUpdate.getBirthdayday());
			}else if(ApplySystemField.IdentityInfo.id == systemFieldId){//身份信息
				applierFromDatabase.setUserIdentityCardType(applierUpdate.getUserIdentityCardType());
				applierFromDatabase.setUserIdentityCode(applierUpdate.getUserIdentityCode());
			}else if(ApplySystemField.EmergencyContactPerson.id == systemFieldId){//紧急联系人
				applierFromDatabase.setEmergencyContactPersonName(applierUpdate.getEmergencyContactPersonName());
				applierFromDatabase.setEmergencyContactPersonPhone(applierUpdate.getEmergencyContactPersonPhone());				
			}else if(ApplySystemField.MailAddress.id == systemFieldId){//通讯地址
				applierFromDatabase.setMailAddress(applierUpdate.getMailAddress());
				applierFromDatabase.setMailCity(applierUpdate.getMailCity());
				applierFromDatabase.setMailProvince(applierUpdate.getMailProvince());			
			}else if(ApplySystemField.HouseAddress.id == systemFieldId){//居住地
				applierFromDatabase.setHouseAddress(applierUpdate.getHouseAddress());
				applierFromDatabase.setHouseCity(applierUpdate.getHouseCity());
				applierFromDatabase.setHouseProvince(applierUpdate.getHouseProvince());							
			}else{
				SasUtil.setObjectProperty(applierFromDatabase, field.propertyName, 
						SasUtil.readObjectProperty(applierUpdate, field.propertyName, null));
			}
		}
		return applierFromDatabase;
	}
	
	/*****************
	 * 生成订单的签名， 用于邮件的查看
	 * @param order
	 * @return
	 */
	public static final String generateOrderSignature(final SasMenuActivityOrder order){
		if(order == null){
			return "";
		}
		final String text = order.getOrderCode() + order.getSasId() + order.getId();
		return MD5SignUtil.generateSignature(Md5Salt.ActivityOrderSign, text, 40);
	}
	
	/***********************
	 * 解析团队成员信息
	 * @param teamInfoUrl
	 * @return
	 */
	public static final SasMenuActivityOrderApplierExcelDetail parseTeamApplierFromExcelFile(final SasMenuActivity activity,
			final List<SasPrivateApplyField> privateFields, final String teamInfoUrl, final boolean isEnglishVersion)
	{
		if(StringUtils.isBlank(teamInfoUrl)){
			return new SasMenuActivityOrderApplierExcelDetail(new ArrayList<SasMenuActivityOrderApplier>(0), 0 , null, 0, 0);
		}
		final byte[] bytes = IOUtil.readFromHttpURL(null, false, teamInfoUrl, 5);
		if(ArrayUtils.isEmpty(bytes)){
			return new SasMenuActivityOrderApplierExcelDetail(new ArrayList<SasMenuActivityOrderApplier>(0), 0 , null, 0, 0);
		}
		List<String[]> appliers = null;
		try{
			appliers = ExcelUtil.readTeamApplierFromExcel(new ByteArrayInputStream(bytes),
					IOUtil.is2007XlsFileName(teamInfoUrl));
		}catch(Exception ex){
			logger.error("Fail to readTeamApplierFromExcel: teamInfoUrl=" + teamInfoUrl + ", ex=" + ex.getMessage(), ex);
			return new SasMenuActivityOrderApplierExcelDetail(new ArrayList<SasMenuActivityOrderApplier>(0), 0 , null, 0, 0);
		}
		if(CollectionUtils.isEmpty(appliers) || appliers.size() < 2){//第一列为标题
			return new SasMenuActivityOrderApplierExcelDetail(new ArrayList<SasMenuActivityOrderApplier>(0), 2 , null, 0, 0);
		}
		//转成名字为key的值
		final Map<Long, SasPrivateApplyField> privateFieldMap = CollectionUtils.extractConllectionToMap(privateFields, "id");
		final SasFieldsWrapper<SasApplyFieldDTO> fieldWrapper = ActivityOrderApplierUtil.parseApplyFields(activity.getOptionalField(),
				activity.getMandatoryField(), privateFieldMap, true, true, isEnglishVersion,
				ActivityStyleClassSwitch.SupportChildPrice.isMe(activity.getStyleClassSwitch()), false);
		final Map<String, SasApplyFieldDTO> fieldMap = new HashMap<String, SasApplyFieldDTO>();
		final Set<Long> requireFieldIdSet = new HashSet<Long>();
		for(final List<SasApplyFieldDTO> fields : new List[]{fieldWrapper.getOptionalFields(), fieldWrapper.getMandatoryFields()}){//必填字段在后，优先
			for(final SasApplyFieldDTO field : fields){
				fieldMap.put(field.getFieldName().replaceAll("(\\(.+\\))|(（.+）)", "").trim(), field);
				if(field.isRequired()){
					requireFieldIdSet.add(field.getId());
				}
			}
		}
		//生成字段列表
		final SasApplyFieldDTO[] fields = new SasApplyFieldDTO[appliers.get(0).length];
		final Set<Long> allNormalExcelFieldIdSet = new HashSet<Long>(); //普通字段
		final Map<Integer, ChineseLanguageValue> specialFieldMapByIndex = new HashMap<Integer, ChineseLanguageValue>();//特殊字段
		for(int index=0; index<appliers.get(0).length; index++ )
		{
			final String excelColumnName = appliers.get(0)[index].replaceAll("(\\(.+\\))|(（.+）)", "").trim();
			final ChineseLanguageValue specialField = ChineseLanguageValue.getByName(excelColumnName);
			if(specialField != null){
				specialFieldMapByIndex.put(index, specialField);
				allNormalExcelFieldIdSet.add(specialField.systemField.id);
			}else{
				fields[index] = fieldMap.get(excelColumnName);
				if(fields[index] != null){
					allNormalExcelFieldIdSet.add(fields[index].getId());
				}
			}
		}

		//确保excel包含了所有必填字段
		for(final SasApplyFieldDTO requiredField : fieldWrapper.getMandatoryFields()){
			if(! allNormalExcelFieldIdSet.contains(requiredField.getId())){
				return new SasMenuActivityOrderApplierExcelDetail(new ArrayList<SasMenuActivityOrderApplier>(0),
						0 , requiredField, 0, 0);
			}
		}
		//开始解析报名人的信息
		final List<SasMenuActivityOrderApplier>  result = new LinkedList<SasMenuActivityOrderApplier>();
		int index = 0;
		int rowIndex = 0, firstErrorRowIndex=0; //前面没有错误的最大的正确的行的下标
		int childApplierCount = 0, auditApplierCount = 0;
		for(final String[] excelFieldValues : appliers)
		{
			if(index++ < 1){ //跳过第一个
				continue;
			}
			final List<BinaryEntry<String, String>> fieldValues = new LinkedList<BinaryEntry<String, String>>();
			boolean isRequireFieldNotInput = false;
			IdentityCardType identityCardType = IdentityCardType.IdCard;
			for(int i=0; i<excelFieldValues.length; i++)
			{
				final ChineseLanguageValue specialField = specialFieldMapByIndex.get(i);
				if(specialField != null)
				{
					if(specialField.systemField == ApplySystemField.PersonType){
						final ApplierPersonType applierType = ApplierPersonType.parse(excelFieldValues[i]);
						fieldValues.add(new BinaryEntry<String, String>(specialField.propertyName, 
								applierType.nameByLanguage(isEnglishVersion)));
					}else if(specialField.systemField == ApplySystemField.IdentityInfo){
						if(specialField == ChineseLanguageValue.IdentityTypeName){
							identityCardType = IdentityCardType.parseByName(excelFieldValues[i]);
							fieldValues.add(new BinaryEntry<String, String>(specialField.propertyName,
									String.valueOf(identityCardType.type)));
						}else{
							if(requireFieldIdSet.contains(ApplySystemField.IdentityInfo.id) && (StringUtils.isBlank(excelFieldValues[i])))
							{
								excelFieldValues[i] = "";
								isRequireFieldNotInput = true;
								break;						
							}
							fieldValues.add(new BinaryEntry<String, String>(specialField.propertyName, excelFieldValues[i]));
						}
					}else if(specialField.systemField == ApplySystemField.EmergencyContactPerson){
						if(requireFieldIdSet.contains(ApplySystemField.EmergencyContactPerson.id)
								&& StringUtils.isBlank(excelFieldValues[i])){
							excelFieldValues[i] = "";
							isRequireFieldNotInput = true;
							break;	
						}
						fieldValues.add(new BinaryEntry<String, String>(specialField.propertyName, excelFieldValues[i] == null ? "" : excelFieldValues[i]));
					}else if(specialField.systemField == ApplySystemField.HouseAddress){
						if(specialField != ChineseLanguageValue.HouseAddressDetail 
								&& requireFieldIdSet.contains(ApplySystemField.HouseAddress.id) 
								&& StringUtils.isBlank(excelFieldValues[i])){
							excelFieldValues[i] = "";
							isRequireFieldNotInput = true;
							break;	
						}
						fieldValues.add(new BinaryEntry<String, String>(specialField.propertyName, excelFieldValues[i] == null ? "" : excelFieldValues[i]));
					}else if(specialField.systemField == ApplySystemField.MailAddress){
						if(specialField != ChineseLanguageValue.MailAddressDetail 
								&& requireFieldIdSet.contains(ApplySystemField.MailAddress.id)
								&& StringUtils.isBlank(excelFieldValues[i])){
							excelFieldValues[i] = "";
							isRequireFieldNotInput = true;
							break;	
						}
						fieldValues.add(new BinaryEntry<String, String>(specialField.propertyName, excelFieldValues[i] == null ? "" : excelFieldValues[i]));
					}			
				}else if(fields[i] != null){
					if(fields[i].isRequired() && StringUtils.isBlank(excelFieldValues[i])){//必填字段不能为空
						//生日和年龄是可以填充的
						if(!((ApplySystemField.Sex.isMe(fields[i].getId()) || ApplySystemField.Birthday.isMe(fields[i].getId()))
								&& requireFieldIdSet.contains(ApplySystemField.IdentityInfo.id))){
							isRequireFieldNotInput = true;
							break;							
						}
					}
					if(ApplySystemField.isSystemField(fields[i].getId())) {
						if(ApplySystemField.Sex.id == fields[i].getId()){		
							fieldValues.add(new BinaryEntry<String, String>(fields[i].getPropertyName(), String.valueOf(SexType.parse(excelFieldValues[i]))));
						}else if(ApplySystemField.Height.id == fields[i].getId()){
							double v = IdUtil.convertToDouble(excelFieldValues[i], 0);
							if(v < 10){
								v = v * 100;
							}
							fieldValues.add(new BinaryEntry<String, String>(fields[i].getPropertyName(), String.valueOf((int)v)));
						}else if(ApplySystemField.Weight.id == fields[i].getId() 
								|| ApplySystemField.Age.id == fields[i].getId()){
							fieldValues.add(new BinaryEntry<String, String>(fields[i].getPropertyName(), String.valueOf(IdUtil.convertToInteger(excelFieldValues[i], 0))));
						}else if(ApplySystemField.Education.id == fields[i].getId()){
							final String value = isEnglishVersion ? EducationLevel.parse(excelFieldValues[i]).englishName 
									: EducationLevel.parse(excelFieldValues[i]).name;
							if(value.equalsIgnoreCase(excelFieldValues[i])){
								fieldValues.add(new BinaryEntry<String, String>(fields[i].getPropertyName(), value));	
							}else{
								fieldValues.add(new BinaryEntry<String, String>(fields[i].getPropertyName(), excelFieldValues[i]));	
							}
						}else{
							fieldValues.add(new BinaryEntry<String, String>(fields[i].getPropertyName(), excelFieldValues[i]));
						}					
					}else{
						fieldValues.add(new BinaryEntry<String, String>(String.valueOf(fields[i].getId()), excelFieldValues[i]));
					}
				}					
			}
			final SasMenuActivityOrderApplier applier = isRequireFieldNotInput ? null : ActivityOrderApplierUtil.parseApplierFieldValues(fieldValues);
			rowIndex ++;
			if(applier != null){
				result.add(applier);				
				if(ApplierPersonType.parse(applier.getPersonType()) == ApplierPersonType.Child){
					childApplierCount ++;
				}else{
					auditApplierCount ++;
				}
			}else{
				if(firstErrorRowIndex < 1){
					firstErrorRowIndex = rowIndex;
				}
			}
		}
		return new SasMenuActivityOrderApplierExcelDetail(result, firstErrorRowIndex, null, childApplierCount, auditApplierCount);
	}
	
	/***************
	 * 拼接身份证号， 用于显示给客户
	 * @param appliers
	 * @return
	 */
	public static final String createUserIdentityCodeString(final List<SasMenuActivityOrderApplier> appliers)
	{
		if(CollectionUtils.isEmpty(appliers)){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");		
		final Set<String> codes = new HashSet<String>();
		for(SasMenuActivityOrderApplier applier : appliers){
			final String code = applier.getUserIdentityCode();
			if(code == null || code.length() < 1 || codes.contains(code)){
				continue;
			}
			codes.add(code);
			if(sb.length() > 0){
				sb.append(DividerChar.ChineseComma.chars + code);
			}else{
				sb.append(code);
			}
		}
		return sb.toString();
	}
	
	/*******************
	 * 确认报名信息是否符合要求， 符合要求的话返回null,否则返回错误信息
	 * @param sas
	 * @param activity
	 * @param appliers
	 * @param applyAttachments
	 * @return
	 */
	public static final ActivityApplierVerifyResult getErrorIfActivityInformationInValid(final boolean isEnglish, 
			final SasMenuActivity activity, 
			final SasMenuActivityStyleClass selectedStyleClass, final List<SasMenuActivityOrderApplier> appliers,
			final SasMenuActivityAttachment[] applyAttachments, final SasFieldsWrapper<SasApplyFieldDTO> fieldWrapper,
			final List<SasMenuActivityAdditionalService> additionalServiceList,
			final SasMenuActivityOrderService sasMenuActivityOrderService, final boolean isTeamApply)
	{
		if(activity.getApplyAttachmentState() == TernaryState.Yes.state && ArrayUtils.isEmpty(applyAttachments)){
			return new ActivityApplierVerifyResult(false, 
					SasUtil.getMsgByLanguage(isEnglish, "对不起，报名需要上传附件， 请上传附件！", "Sorry, please upload attachments"),
					null);
		}		
		if(CollectionUtils.isEmpty(appliers)){
			return new ActivityApplierVerifyResult(false, 
					SasUtil.getMsgByLanguage(isEnglish, "对不起，请填写报名人信息！", "Sorry, please enter applier's information"),
					null);
		}
		//团队报名确认
		if(isTeamApply){
			if(appliers.size() < activity.getTeamApplyMinUserCount() || (activity.getTeamApplyMaxUserCount() > 1 && appliers.size() > activity.getTeamApplyMaxUserCount())){
				if(activity.getTeamApplyMaxUserCount() > 1){
					return new ActivityApplierVerifyResult(false, 
							SasUtil.getMsgByLanguage(isEnglish, "对不起，团队报名人数范围为", "Sorry, team appliers count must be in range ")
							+ MathUtil.max(activity.getTeamApplyMinUserCount(), 0) + " - " + activity.getTeamApplyMaxUserCount()
							+ "！",
							null);
				}else{
					return new ActivityApplierVerifyResult(false, 
							SasUtil.getMsgByLanguage(isEnglish, "对不起，团队报名人数必须至少为", "Sorry, team appliers count must be greater than ")
							+ activity.getTeamApplyMinUserCount() + "！",
							null);
				}
			}
		}else{//个人报名
			if(activity.getMaxApplier() > 0 && activity.getMaxApplier() < appliers.size() && selectedStyleClass.getFixedAmount() == BinaryState.No.state){
				return new ActivityApplierVerifyResult(false, 
						SasUtil.getMsgByLanguage(isEnglish,  "对不起，报名人数最多不能超过"+ activity.getMaxApplier()+"人！", "Sorry, appliers count can't excess "
						+ activity.getMaxApplier() + "!"),
						null);
			}
			if (selectedStyleClass.getFixedAmount() == BinaryState.Yes.state && (appliers.size() < selectedStyleClass.getApplyMinUserCount() ||
          appliers.size() > MathUtil.minInt(selectedStyleClass.getApplyMaxUserCount(), selectedStyleClass.getStoreCount()))){
			  return new ActivityApplierVerifyResult(false,
						SasUtil.getMsgByLanguage(isEnglish, "对不起，报名余位不足",
            "Sorry, the number of applicants is not enough!"), null);
      }
		}
		//必填字段信息核实
		//报名人限制
		final ActivityApplierLimitDTO applierLimit = ActivityApplierLimitDTO.parseLimitValue(activity.getApplierLimit());
		Calendar styleClassTime4ApplierLimit = null;
		if(applierLimit != null){			
			styleClassTime4ApplierLimit = Calendar.getInstance();
			if(selectedStyleClass != null && selectedStyleClass.getStartTime() > 0){
				styleClassTime4ApplierLimit.setTimeInMillis(selectedStyleClass.getStartTime());
			}else{
				styleClassTime4ApplierLimit.setTimeInMillis(activity.getStartTime());
			}
		}
		//解析报名字段
		final List<String> identityCodeTypeList = new LinkedList<String>();
		final List<String> identityCodeList = new LinkedList<String>();
		final Set<String> identityCodeAndTypeSet = new HashSet<String>();
		for(final SasMenuActivityOrderApplier applier : appliers)
		{
			if(applier.getUserIdentityCardType() == IdentityCardType.IdCard.type
					&& StringUtils.isNotBlank(applier.getUserIdentityCode())
					&& !ValidatorUtil.cardNoValidate(applier.getUserIdentityCode())){
				return new ActivityApplierVerifyResult(false, 
						SasUtil.getMsgByLanguage(isEnglish, "对不起，错误的身份证号码【" + applier.getUserIdentityCode() + "】，请修改后再提交！", 
						"Sorry, error user identity code "+applier.getUserIdentityCode()+ ", please change and submit again!"),
						identityCodeAndTypeSet);	
			}
			//部分人把手机号码写到了紧急联系人的姓名上面
			if(StringUtils.isNotBlank(applier.getEmergencyContactPersonName()) && ValidatorUtil.mobileORTelephoneValidate(applier.getEmergencyContactPersonName())){
				return new ActivityApplierVerifyResult(false, 
						SasUtil.getMsgByLanguage(isEnglish, "请输入正确的紧急联系人姓名！",
						"Please enter the valid emergency contact person name!"),
						identityCodeAndTypeSet);	
			}
			if(StringUtils.isNotBlank(applier.getEmergencyContactPersonPhone()) && !ValidatorUtil.mobileORTelephoneValidate(applier.getEmergencyContactPersonPhone())){
				return new ActivityApplierVerifyResult(false, 
						SasUtil.getMsgByLanguage(isEnglish, "请输入正确的紧急联系人手机号码！",
						"Please enter the valid emergency contact person mobile phone!"),
						identityCodeAndTypeSet);	
			}
			final Map<Long, String> map = ActivityOrderApplierUtil.generateApplierFieldValues(applier, 
					fieldWrapper.getAllFieldIds(), true, false, isEnglish, true).value;
			for(final SasApplyFieldDTO necessaryField : fieldWrapper.getMandatoryFields())
			{
				final long fieldId = necessaryField.getId();
				String v = map.get(fieldId);
				if(v != null){
					v = v.trim();				
				}
				if(StringUtils.isBlank(v) || DividerChar.ActivityFieldValue.chars.equals(v) 
					|| (fieldId == ApplySystemField.EmergencyContactPerson.id && (StringUtils.isBlank(applier.getEmergencyContactPersonName()) || StringUtils.isBlank(applier.getEmergencyContactPersonPhone()))
					|| (fieldId == ApplySystemField.HouseAddress.id && (StringUtils.isBlank(applier.getHouseProvince()) || StringUtils.isBlank(applier.getHouseProvince())))
					|| (fieldId == ApplySystemField.MailAddress.id) && (StringUtils.isBlank(applier.getMailProvince()) || StringUtils.isBlank(applier.getMailProvince())))
					|| ((fieldId == ApplySystemField.Age.id || fieldId == ApplySystemField.Height.id 
							|| fieldId == ApplySystemField.Weight.id) && IdUtil.convertToInteger(v, 0) < 1)
					|| (fieldId == ApplySystemField.Birthday.id && applier.getBirthdayYear() < 1800)
				){
					return new ActivityApplierVerifyResult(false, 
							SasUtil.getMsgByLanguage(isEnglish, "对不起，请填写【" + necessaryField.getFieldName() + "】字段信息后再提交！", 
							"Sorry, please enter applier information "+ necessaryField.getFieldName()+ "and submit again!"),
							identityCodeAndTypeSet);	
				}
				//确认自定义选择型字段的值: 是否在选项之内
				if(necessaryField.getType() == FieldValueType.RadioSelect.type && (fieldId == ApplySystemField.BloodType.id
						|| fieldId == ApplySystemField.Education.id || fieldId == ApplySystemField.ClothSize.id
						|| !ApplySystemField.isSystemField(fieldId))){
					final String[] values = necessaryField.getOptionValues();
					if(ArrayUtils.isNotEmpty(values))
					{
						if(!CollectionUtils.containValueByArray(values, v, true)){
							return new ActivityApplierVerifyResult(false, 
									SasUtil.getMsgByLanguage(isEnglish, "对不起，【" + necessaryField.getFieldName() 
											+ "】字段值\""+v+"\"必须选自：", 
									"Sorry, field "+ necessaryField.getFieldName()+ "'s value '"+v+"' must be one of:")
									+ CollectionUtils.joinStringArray(values, DividerChar.Comma),
									identityCodeAndTypeSet);	
						}
					}
				}				
			}
			//检查可选字段, 确认自定义选择型字段的值: 是否在选项之内
			for(final SasApplyFieldDTO optionalField : fieldWrapper.getOptionalFields())
			{
				final long fieldId = optionalField.getId();
				String v = map.get(fieldId);
				if(v != null){
					v = v.trim();				
				}
				if(optionalField.getType() == FieldValueType.RadioSelect.type && (fieldId == ApplySystemField.BloodType.id
						|| fieldId == ApplySystemField.Education.id || fieldId == ApplySystemField.ClothSize.id
						|| !ApplySystemField.isSystemField(fieldId))){
					final String[] values = optionalField.getOptionValues();
					if(ArrayUtils.isNotEmpty(values) && StringUtils.isNotBlank(v))
					{
						if(!CollectionUtils.containValueByArray(values, v, true)){
							return new ActivityApplierVerifyResult(false, 
									SasUtil.getMsgByLanguage(isEnglish, "对不起，【" + optionalField.getFieldName() 
											+ "】字段值\""+v+"\"必须选自：", 
									"Sorry, field "+ optionalField.getFieldName()+ "'s value '"+v+"' must be one of:")
									+ CollectionUtils.joinStringArray(values, DividerChar.Comma),
									identityCodeAndTypeSet);	
						}
					}
				}
			}
			if(StringUtils.isNotBlank(applier.getEmail()) && !ValidatorUtil.emailValidate(applier.getEmail())){
				return new ActivityApplierVerifyResult(false, 
						SasUtil.getMsgByLanguage(isEnglish,  "对不起，报名人的邮箱有误:", 
						"Sorry, error applier's email:") + applier.getEmail(),
						identityCodeAndTypeSet);
			}

			if(StringUtils.isNotBlank(applier.getUserIdentityCode()))
			{
				if(activity.getApplierIdentityCodeUniqueType() == BinaryState.Yes.state){
					final String codeAndType = applier.getUserIdentityCode() + "-" + applier.getUserIdentityCardType();
					if(identityCodeAndTypeSet.contains(codeAndType)){
						return new ActivityApplierVerifyResult(false, 
								SasUtil.getMsgByLanguage(isEnglish,  "对不起，报名人证件号码重复:", 
								"Sorry, duplicate applier's identity code:") + applier.getUserIdentityCode(),
								identityCodeAndTypeSet);
					}
					identityCodeList.add(applier.getUserIdentityCode());
					identityCodeTypeList.add(String.valueOf(applier.getUserIdentityCardType()));
					identityCodeAndTypeSet.add(codeAndType);
				}
				//年龄判读
				if(applier.getUserIdentityCardType() == IdentityCardType.IdCard.type)
				{
					final FourEntry<Integer, Integer, Integer, SexType> identityBirthdayAndSex = UserUtil.parseBirthdayFromUserIdentityCode(applier.getUserIdentityCode());
					if(identityBirthdayAndSex != null)
					{
						applier.setBirthdayYear(identityBirthdayAndSex.firstValue);
						applier.setBirthdayMonth(identityBirthdayAndSex.secondValue - 1);
						applier.setBirthdayday(identityBirthdayAndSex.thirdValue);
						applier.setSex(identityBirthdayAndSex.forthValue.type);
					}	
					if(applierLimit != null)
					{
						if(applierLimit.getType() == ApplierLimitType.Birthday){
							if(!applierLimit.isAgeInvalidAccordingToIdentityCardByBirthdayLimit(identityBirthdayAndSex)){
								return new ActivityApplierVerifyResult(false, 
										SasUtil.getMsgByLanguage(isEnglish, 
										"对不起，报名人生日范围为: ", "Sorry, applier's birthday must between: ")
										+ applierLimit.getBirthdayLimitString(isEnglish),
										identityCodeAndTypeSet);
							}
						}else if(!applierLimit.isAgeInvalidAccordingToIdentityCardByAgeLimit(identityBirthdayAndSex, styleClassTime4ApplierLimit)){
							return new ActivityApplierVerifyResult(false, 
									SasUtil.getMsgByLanguage(isEnglish, "对不起，报名人年龄范围为: ", "Sorry, applier's age must between: ")
										+ applierLimit.getAgeLimitString(isEnglish),
										identityCodeAndTypeSet);
						}
					}				
				}			
			}
		}

		//确保身份证唯一性
		final List<SasMenuActivityOrderApplier> duplicateAppliers = ActivityOrderApplierUtil.getDuplicateIdentityCodeOnePaidApplier(sasMenuActivityOrderService,
				activity, identityCodeTypeList,  identityCodeList);
		if(CollectionUtils.isNotEmpty(duplicateAppliers)){
			final String str = ActivityOrderApplierUtil.createUserIdentityCodeString(duplicateAppliers);
			return new ActivityApplierVerifyResult(false, SasUtil.getMsgByLanguage(isEnglish, "对不起，这些证件号已经报名，请修改后再次提交：", 
					"Sorry, these ID card has already applied, please change and applied again:") + str + "！", 
					identityCodeAndTypeSet);
		}
		//确认附件服务库存是否足够
		if(CollectionUtils.isNotEmpty(additionalServiceList)){
			for(final SasMenuActivityAdditionalService service : additionalServiceList)
			{
				if(service.getStoreCount() < appliers.size()){
					return new ActivityApplierVerifyResult(false, SasUtil.getMsgByLanguage(isEnglish, "对不起，附加服务【" + service.getTitle() + "】库存不足，仅剩", 
							"Sorry, the additional service 【" + service.getTitle() + "】's store is not enough, current store is") + service.getStoreCount() + "！", 
							identityCodeAndTypeSet);
				}
			}
		}
		return new ActivityApplierVerifyResult(true, null, identityCodeAndTypeSet);
	}
	
	/*************
	 * 如果开启了同一个身份证只能一个人包括付款的话， 通过此函数做判断
	 * @param sas
	 * @param sasMenuActivityOrderService
	 * @param activity
	 * @param identityCodeTypeList
	 * @param identityCodeList
	 * @return
	 */
	public static final List<SasMenuActivityOrderApplier> getDuplicateIdentityCodeOnePaidApplier(final SasMenuActivityOrderService sasMenuActivityOrderService,
			final SasMenuActivity activity, final List<String> identityCodeTypeList,  final List<String> identityCodeList)
	{
		if(activity.getApplierIdentityCodeUniqueType() == BinaryState.Yes.state && CollectionUtils.isNotEmpty(identityCodeList)){
			return sasMenuActivityOrderService.listAppliersByIdentityCode(activity.getId(), 
					identityCodeTypeList, identityCodeList);
		}
		return null;
	}
	
	public static final List<SasMenuActivityOrderApplier> getDuplicateIdentityCodeOnePaidApplier(final SasMenuActivityOrderService sasMenuActivityOrderService,
			final SasMenuActivity activity, final SasMenuActivityOrder order)
	{
		if(activity != null && activity.getApplierIdentityCodeUniqueType() == BinaryState.Yes.state){			
			return ActivityOrderApplierUtil.getDuplicateIdentityCodeOnePaidApplier(sasMenuActivityOrderService,
					activity, sasMenuActivityOrderService.listAppliersByOrder(order.getId()));
		}
		return null;
	}
	
	/*******************
	 * 确认相同身份证号码是否已经报名
	 * @param sas
	 * @param appliers
	 * @return
	 */
	public static final List<SasMenuActivityOrderApplier> getDuplicateIdentityCodeOnePaidApplier(
			final SasMenuActivityOrderService sasMenuActivityOrderService,
			final SasMenuActivity activity, final List<SasMenuActivityOrderApplier> appliers)
	{
		if(CollectionUtils.isEmpty(appliers)){
			return null;
		}
		if(activity.getApplierIdentityCodeUniqueType() == BinaryState.Yes.state){			
			final List<String> identityCodeTypeList = new LinkedList<String>();
			final List<String> identityCodeList = new LinkedList<String>();
			for(final SasMenuActivityOrderApplier applier : appliers)
			{
				if(StringUtils.isNotBlank(applier.getUserIdentityCode())){
					identityCodeList.add(applier.getUserIdentityCode());
					identityCodeTypeList.add(String.valueOf(applier.getUserIdentityCardType()));
				}
			}
			return sasMenuActivityOrderService.listAppliersByIdentityCode(activity.getId(), identityCodeTypeList, identityCodeList);
		}
		return null;
	}

	/***********
	 * 检查自定义字段的值是否正确
	 * @param applier
	 * @param applyFields
	 * @return
	 */
	public static final Map<Long, String> checkAllSystemFieldValuesIfCorrect(final Map<Long, String> applier,
			final SasFieldsWrapper<SasApplyFieldDTO> applyFields)
	{
		if(CollectionUtils.isNotEmpty(applyFields.getMandatoryFields()))
		{
			for(final SasApplyFieldDTO f : applyFields.getMandatoryFields())
			{
				if(ApplySystemField.isSystemField(f.getId())
						|| f.getType() != FieldValueType.RadioSelect.type){//跳过系统字段或者非单选字段
					continue;
				}
				final String value = applier.get(f.getId());
				if(value == null){
					continue;
				}
				final String v = CollectionUtils.findFirstStringValue(f.getOptionValues(), value, true);
				if(v == null){
					applier.remove(f.getId());
				}else{
					applier.put(f.getId(),  v);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(applyFields.getOptionalFields()))
		{
			for(final SasApplyFieldDTO f : applyFields.getOptionalFields())
			{
				if(ApplySystemField.isSystemField(f.getId())
						|| f.getType() != FieldValueType.RadioSelect.type){//跳过系统字段或者非单选字段
					continue;
				}
				final String value = applier.get(f.getId());
				if(value == null){
					continue;
				}
				final String v = CollectionUtils.findFirstStringValue(f.getOptionValues(), value, true);
				if(v == null){
					applier.remove(f.getId());
				}else{
					applier.put(f.getId(),  v);
				}
			}
		}
		return applier;
	}
	
	/**************
	 * 一个活动下面的订单允许自定义两个显示字段，这里设置值， 设置到字段attachment3Name和attachment4Name
	 * @param orders
	 * @param applierMapByOrder
	 * @param displayApplierIdPair
	 */
	public static final void initOrderDisplayField2Attachment34FieldValue(final List<SasMenuActivityOrder> orders, final Map<Long, List<SasMenuActivityOrderApplier>> applierMapByOrder,
			final BinaryEntry<Long, Long> displayApplierIdPair)
	{
		if(CollectionUtils.isEmpty(orders)){
			return;
		}
		if(displayApplierIdPair.key >= 0)
		{	
			for(final SasMenuActivityOrder order : orders)
			{
				//第一个字段
				if(ApplySystemField.isNotApplyField(displayApplierIdPair.key)){
					order.setAttachment3Name(ActivityOrderApplierUtil.generateOrderOneFieldValue(order, displayApplierIdPair.key));
				}else{
					final Set<String> fieldValues = new HashSet<String>();
					boolean moreFieldValues = false;
					final List<SasMenuActivityOrderApplier> appliers = applierMapByOrder == null ? null : applierMapByOrder.get(order.getId());
					if(CollectionUtils.isNotEmpty(appliers))
					{
						for(final SasMenuActivityOrderApplier applier : appliers)
						{
							final String fieldValue = ActivityOrderApplierUtil.generateApplierOneFieldValue(applier, displayApplierIdPair.key, false);
							if(fieldValue != null && fieldValue.length() > 0){
								fieldValues.add(fieldValue.trim());
								if(fieldValues.size() >= 5){
									moreFieldValues = true;
									break;
								}
							}
						}
					}
					if(fieldValues.size() < 1 && ApplySystemField.Nickname.isMe(displayApplierIdPair.key)){
						fieldValues.add(order.getUserNickname());
					}
					if(fieldValues.size() > 0){
						order.setAttachment3Name(CollectionUtils.joinStringArray(fieldValues, DividerChar.ChineseComma.chars) + (moreFieldValues?"等":""));
					}else{
						order.setAttachment3Name("");
					}
				}
				
				//第二个字段
				if(displayApplierIdPair.value < 0){
					order.setAttachment4Name("");
				}else if(ApplySystemField.isNotApplyField(displayApplierIdPair.value)){
					order.setAttachment4Name(ActivityOrderApplierUtil.generateOrderOneFieldValue(order, displayApplierIdPair.value));
				}else{
					final Set<String> fieldValues = new HashSet<String>();
					boolean moreFieldValues = false;
					final List<SasMenuActivityOrderApplier> appliers = applierMapByOrder == null ? null : applierMapByOrder.get(order.getId());
					if(CollectionUtils.isNotEmpty(appliers))
					{
						for(final SasMenuActivityOrderApplier applier : appliers)
						{
							final String fieldValue = ActivityOrderApplierUtil.generateApplierOneFieldValue(applier, displayApplierIdPair.value, false);
							if(fieldValue != null && fieldValue.length() > 0){
								fieldValues.add(fieldValue.trim());
								if(fieldValues.size() >= 5){
									moreFieldValues = true;
									break;
								}
							}
						}
					}
					if(fieldValues.size() < 1 && ApplySystemField.Nickname.isMe(displayApplierIdPair.value)){
						fieldValues.add(order.getUserNickname());
					}
					if(fieldValues.size() > 0){
						order.setAttachment4Name(CollectionUtils.joinStringArray(fieldValues, DividerChar.ChineseComma.chars)+ (moreFieldValues?"等":""));
					}else{
						order.setAttachment4Name("");
					}
				}
			}	
		}else{
			for(final SasMenuActivityOrder order : orders){
				order.setAttachment3Name("");
				order.setAttachment4Name("");
			}
		}
	}
	
}