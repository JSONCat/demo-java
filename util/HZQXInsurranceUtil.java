/**
 * 
 */
package com.sas.core.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import com.huize.qixin.api.model.info.Applicant;
import com.huize.qixin.api.model.info.AreaInfo;
import com.huize.qixin.api.model.info.AttributeModule;
import com.huize.qixin.api.model.info.AttributeValue;
import com.huize.qixin.api.model.info.Insurant;
import com.huize.qixin.api.model.info.InsureAttribute;
import com.huize.qixin.api.model.info.OrderDetail;
import com.huize.qixin.api.model.info.OtherInfo;
import com.huize.qixin.api.model.info.Product;
import com.huize.qixin.api.model.info.ProductAttribute;
import com.huize.qixin.api.model.info.ProductDestination;
import com.huize.qixin.api.model.info.ProductInsuredJob;
import com.huize.qixin.api.model.info.RestrictDictionary;
import com.huize.qixin.api.model.info.RestrictGene;
import com.huize.qixin.api.req.insure.InsureReq;
import com.huize.qixin.api.req.order.OrderDetailReq;
import com.huize.qixin.api.req.pay.LocalPayReq;
import com.huize.qixin.api.req.policy.PolicyUrlReq;
import com.huize.qixin.api.req.product.ProductDestinationReq;
import com.huize.qixin.api.req.product.ProductDetailReq;
import com.huize.qixin.api.req.product.ProductInsureAttrReq;
import com.huize.qixin.api.req.product.ProductInsuredAreaReq;
import com.huize.qixin.api.req.product.ProductInsuredJobReq;
import com.huize.qixin.api.req.product.ProductListReq;
import com.huize.qixin.api.req.product.PropertyAddressReq;
import com.huize.qixin.api.req.surrender.SurrenderPolicyReq;
import com.huize.qixin.api.req.trial.SimpleTrialReq;
import com.huize.qixin.api.resp.insure.InsureResp;
import com.huize.qixin.api.resp.order.OrderDetailResp;
import com.huize.qixin.api.resp.pay.LocalPayResp;
import com.huize.qixin.api.resp.policy.PolicyUrlResp;
import com.huize.qixin.api.resp.product.ProductDestinationResp;
import com.huize.qixin.api.resp.product.ProductDetailResp;
import com.huize.qixin.api.resp.product.ProductInsureAttrResp;
import com.huize.qixin.api.resp.product.ProductInsuredAreaResp;
import com.huize.qixin.api.resp.product.ProductInsuredJobResp;
import com.huize.qixin.api.resp.product.ProductListResp;
import com.huize.qixin.api.resp.product.PropertyAddressResp;
import com.huize.qixin.api.resp.surrender.SurrenderPolicyResp;
import com.huize.qixin.api.resp.trial.SimpleTrialResp;
import com.qixin.openapi.client.OpenApiRemoteOperation;
import com.qixin.openapi.client.common.ProxyFactory;
import com.qixin.openapi.conf.Configure;
import com.qixin.openapi.model.common.CommonResult;
import com.sas.core.constant.CommonConstant.ErrorCode;
import com.sas.core.constant.CommonConstant.SortOrder;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.constant.UserConstant.IdentityCardType;
import com.sas.core.constant.UserConstant.SexType;
import com.sas.core.dto.BinaryEntry;
import com.sas.core.dto.InsurranceCategory;
import com.sas.core.dto.InsurranceCompany;
import com.sas.core.dto.InsurranceProductDetail;
import com.sas.core.dto.InsurranceProductPriceQuery;
import com.sas.core.dto.ProductInsureAttributeSetting;
import com.sas.core.meta.SasInsureUser;
import com.sas.core.meta.SasInsurranceOrder;
import com.sas.core.meta.SasInsurranceOrderApplier;
import com.sas.core.service.EnvironmentService;
import com.sas.core.service.PhoneMessageService;
import com.sas.core.util.HZInsurranceConfigUtil.Module;
import com.sas.core.util.HZInsurranceConfigUtil.PersonalRelation;

/**
 * 惠泽保险
 * @author Administrator
 *
 */
public class HZQXInsurranceUtil {

	private static final Logger logger = Logger.getLogger("com.logger.insurrance");
	
	public static final String TRAN_NO_PREFIX_NORMAL = "HZ";
	public static final String TRAN_NO_PREFIX_ORDER = "OR";
	public static final String TRAN_NO_PREFIX_ORDER_PAY = "OR-PAY";
	public static final String TRAN_NO_PREFIX_ORDER_REFUND = "OR-REFUND";
	
	private static OpenApiRemoteOperation operation = ProxyFactory.create(OpenApiRemoteOperation.class);
	
	/********
	 * 一些常量
	 */
	public static int partnerId = 0;
	public static String key = ""; 
	public static String url = ""; 
	
	//启动时装载的缓存
	private static final List<InsurranceCategory> allCategories = new LinkedList<InsurranceCategory>();
	private static final List<InsurranceCompany> allCompanies = new LinkedList<InsurranceCompany>();	
	private static final List<InsurranceProductDetail> allProductDetails = new LinkedList<InsurranceProductDetail>();
//	private static final ConcurrentHashMap<String, BigDecimal> allSaihuitongDiscountRatioMap = new ConcurrentHashMap<String, BigDecimal>();; //赛会通产品折扣
	private static final ConcurrentHashMap<String, InsurranceProductDetail> productDetailMap = new ConcurrentHashMap<String, InsurranceProductDetail>();
	private static final ConcurrentHashMap<String, List<AreaInfo>> productInsuredAreaMap = new ConcurrentHashMap<String, List<AreaInfo>>(); //可投保区域
	private static final ConcurrentHashMap<String, List<ProductInsuredJob>> productInsuredJobMap = new ConcurrentHashMap<String, List<ProductInsuredJob>>(); //产品对应的职业范围
	private static final ConcurrentHashMap<String, List<ProductDestination>> productInsuredDestinationMap = new ConcurrentHashMap<String, List<ProductDestination>>(); //出行目的地
	private static final ConcurrentHashMap<String, List<AreaInfo>> productPropertyAddressMap = new ConcurrentHashMap<String, List<AreaInfo>>(); //财产地址	//	
	
	//根据产品方案代码查询产品投保属性信息，包含投保参数、校验规则以及约束信息
	private static final ConcurrentHashMap<String, InsureAttribute> productInsureAttributeMap = new ConcurrentHashMap<String, InsureAttribute>(); 
	private static final ConcurrentHashMap<String, ProductInsureAttributeSetting> productInsureAttributeSettingMap = new ConcurrentHashMap<String, ProductInsureAttributeSetting>(); 
	
	private static EnvironmentService environmentService;
	
	/******
	 * 是否是测试的保险环境
	 * @return
	 */
	public static final boolean isTestInsurranceEnvironment(){
		//测试环境： http://tuneapi.qixin18.com/api/
		return HZQXInsurranceUtil.getURL().contains("tuneapi");
	}
	
	public static final EnvironmentService getEnvironmentService(){
		return environmentService;
	}
	
	/************
	 * 启动时的数据装载
	 */
	public static final void init(final EnvironmentService environmentService, final PhoneMessageService phoneService)
	{
		ThreadUtil.execute(new Runnable(){
			public void run(){
				logger.fatal("begin init insurrance data >>>");
				HZQXInsurranceUtil.environmentService = environmentService;
				Configure.Channel.channelKey = HZQXInsurranceUtil.getKey();
				Configure.Request.baseUrl = HZQXInsurranceUtil.getURL();
				Configure.Channel.partnerId = HZQXInsurranceUtil.getPartnerId();
				HZQXInsurranceUtil.doInit(phoneService);
				logger.fatal("finish init insurrance data <<<");
			}
		});
	}
	/**************
	 * 初始化保险
	 * @param phoneService
	 */
	private static final void doInit(final PhoneMessageService phoneService)
	{
		//读取所有产品
		List<Product> products = null;
		int times = 0;
		do{
			try{
				products = HZQXInsurranceUtil.queryAllOnShelfProductList();
			}catch(Exception ex){
				logger.fatal("Fail to init hz insurrance products, ex=" + ex.getMessage(), ex);
			}
			if(products == null){
				times ++;
				phoneService.sendSystemErrorMessage(ErrorCode.InsurranceError, HZQXInsurranceUtil.class, RuntimeException.class);
				ThreadUtil.sleepNoException(Miliseconds.TweentySeconds.miliseconds * times);
			}
		}while(products == null && times < 50);	
		if(CollectionUtils.isEmpty(products)){
			logger.fatal("Fail to init hz insurrance products, No products!");
		}
		//详情
		final List<String> allValidCaseCodes = HZQXInsurranceUtil.doInitAllProductDetails(products);
		CollectionUtils.sortTheList(allCategories, "totalProductCount", SortOrder.DESC);
		CollectionUtils.sortTheList(allCompanies, "totalProductCount", SortOrder.DESC);
		//读取投保时的一些限制, 然后才能计算价格
		for(final String caseCode : allValidCaseCodes)
		{
			HZQXInsurranceUtil.getProductInsureAttribute(caseCode);
		}		
		//初始化价格
		for(final String caseCode : allValidCaseCodes){
			HZQXInsurranceUtil.doInitProductPrice(caseCode);
		}
		//初始化一些投保的开关设置属性
		for(final String caseCode : allValidCaseCodes){
			HZQXInsurranceUtil.getProductInsureAttributeSetting(caseCode);
		}
	}
	
	private static final List<String> doInitAllProductDetails(final List<Product> products)
	{
		//查询每个产品的详情
		final List<String> allCaseCodes = new LinkedList<String>();
		final Map<Integer, InsurranceCategory> allCategoryMap = new HashMap<Integer, InsurranceCategory>();
		final Map<String, InsurranceCompany> allCompanyMap = new HashMap<String, InsurranceCompany>();
		for(final Product p : products)
		{
			final InsurranceProductDetail detail = HZQXInsurranceUtil.queryProductDetail(p, 5);
			if(detail != null){
				allCaseCodes.add(p.getCaseCode());
				//设置分类信息和公司信息
				addCategoryAndCompany(detail, allCategoryMap, allCompanyMap);
			}
		}
		return allCaseCodes;
	}
	
	private static final void addCategoryAndCompany(final InsurranceProductDetail detail, final Map<Integer, InsurranceCategory> allCategoryMap,
			final Map<String, InsurranceCompany> allCompanyMap)
	{
		final boolean isSaiShi = detail.getProductName().indexOf('赛') >= 0;
		//公司
		InsurranceCompany company = allCompanyMap.get(detail.getCompanyCnName());
		if(company == null){
			company = new InsurranceCompany(System.currentTimeMillis(), detail.getCompanyCnName(), 0, 0, 0);
			allCompanyMap.put(detail.getCompanyCnName(), company);
			allCompanies.add(company);
		}
		if(isSaiShi){
			company.setTotalSaishiProductCount(company.getTotalSaishiProductCount() + 1);					
		}else{
			company.setTotalHuwaiProductCount(company.getTotalHuwaiProductCount() + 1);
		}
		company.setTotalProductCount(company.getTotalProductCount() + 1);		
		//分类
		InsurranceCategory category = allCategoryMap.get(detail.getSecondCategory());
		if(category == null){
			category = new InsurranceCategory(detail.getSecondCategory(), 
					HZInsurranceConfigUtil.parseCategoryName(detail.getSecondCategory()), 0, 0, 0);
			allCategoryMap.put(detail.getSecondCategory(), category);
			allCategories.add(category);
		}
		if(isSaiShi){
			category.setTotalSaishiProductCount(category.getTotalSaishiProductCount() + 1);					
		}else{
			category.setTotalHuwaiProductCount(category.getTotalHuwaiProductCount() + 1);
		}
		category.setTotalProductCount(category.getTotalProductCount() + 1);
	} 
	
	//通过惠泽接口查询
	private static final InsurranceProductDetail queryProductDetail(final Product p, final int tryTimes)
	{
		if(tryTimes < 1){
			return null;
		}
		final String transNo = HZQXInsurranceUtil.createTransactionNo();
		try{
			final ProductDetailReq req = new ProductDetailReq();
			req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
			req.setTransNo(transNo);
			req.setCaseCode(p.getCaseCode());
			req.setPlatformType(0); //必填，平台标识 0：PC 1：H5
			final CommonResult<ProductDetailResp> resp = operation.productDetail(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + transNo + ", resp:" + ReflectionToStringBuilder.toString(resp));
				ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
				return queryProductDetail(p, tryTimes-1);
			}
			if(resp.getData().getState() != 1 && resp.getData().getState() != 3){//产品状态=> 0：待审 1：上架 2：下架 3：测试 4：停售
				return null;
			}
			final BigDecimal saihuitongDiscountRatio = HZInsurranceConfigUtil.createSaihuitongDiscountRatioByProductName(p.getProdName());
			//计算折扣和产品详情
//			allSaihuitongDiscountRatioMap.put(p.getCaseCode(), saihuitongDiscountRatio);
			final InsurranceProductDetail detail = new InsurranceProductDetail(p, resp.getData(), saihuitongDiscountRatio);
			productDetailMap.put(p.getCaseCode(), detail);
			allProductDetails.add(detail);
			return detail;
		}catch(Exception ex){
			logger.fatal("tran-no:" + transNo + ", ex:" + ex.getMessage(), ex);
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			return queryProductDetail(p, tryTimes-1);
		}
	}
	
	/*********
	 * 初始化化价格， 由于默认给的价格，跟默认展示的保障期限和年龄会有出入，进行重新计算
	 */
	private static final void doInitProductPrice(final String caseCode)
	{
		final InsurranceProductDetail productDetail = productDetailMap.get(caseCode);
		if(productDetail == null){
			return;
		}
		//获取该产品的属性
		final InsureAttribute insureAttr = HZQXInsurranceUtil.getProductInsureAttribute(caseCode);
		if(insureAttr == null){
			return;
		}
		final Calendar cal = Calendar.getInstance();
		//确定投保日期范围
		final int maxDays= insureAttr.getLatestDate() - insureAttr.getFirstDate();
		final long startDate = cal.getTimeInMillis() + (maxDays/2) * Miliseconds.OneDay.miliseconds; //假定后天开始投保
		//年龄设置为30岁，旺年
		cal.add(Calendar.YEAR, -30);
		final long birthday = cal.getTimeInMillis();
		SimpleTrialResp resp = null;
		if(productDetail.getRestrictGeneInsurantDateLimit() != null && CollectionUtils.isNotEmpty(productDetail.getRestrictGeneInsurantDateLimit().getValues()))
		{
			final RestrictDictionary rd = productDetail.getRestrictGeneInsurantDateLimit().getValues().get(0);
			cal.setTimeInMillis(startDate);
			if(rd.getUnit().contains("年")){
				cal.add(Calendar.YEAR, rd.getMin());
				cal.add(Calendar.DAY_OF_YEAR, -1);				
			}else if(rd.getUnit().contains("天")){
				cal.add(Calendar.DAY_OF_YEAR, rd.getMin() - 1);
			}
			resp = HZQXInsurranceUtil.doOrderTrail(productDetail, birthday, startDate, cal.getTimeInMillis(), 2);
		}else{
			resp = HZQXInsurranceUtil.doOrderTrail(productDetail, birthday, startDate, startDate, 2);
		}		
		if(resp != null && resp.getOriginalPrice() > 0){
			productDetail.resetPriceData(resp.getOriginalPrice());
			productDetail.setBuyQuota(resp.getBuyQuota());
		}		
	}
	
	/***************
	 * 查询产品价格[原价]， 返回元
	 * @param product
	 * @param birthday
	 * @param startDate
	 * @param endDate
	 * @param extMap
	 * @return
	 */
	public static final InsurranceProductPriceQuery queryProductOrderPrice(final InsurranceProductDetail product, final long birthday,
			final long startDate, final long endDate)
	{
		final CommonResult<SimpleTrialResp> resp = HZQXInsurranceUtil.doOrderTrailProxy(product, birthday, startDate, endDate, 2);
		if(resp == null){
			return new InsurranceProductPriceQuery("对不起，系统出错了， 请稍后重试！", null, null);
		}else if(!isReponseSuc(resp.getRespCode())){
			return new InsurranceProductPriceQuery(resp.getRespMsg(), null, null);
		}else{
			final SimpleTrialResp data = resp.getData();
			return new InsurranceProductPriceQuery(null, 
					new BigDecimal(data.getOriginalPrice()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP),
					new BigDecimal(data.getSinglePrice()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP));
		}	
	}
	
	
	/**********************************************一些配置信息********************************************************
	 * 一些配置信息
	 * @return
	 */
	public static String getKey() {
		int times = 0;
		while((key == null || key.length() < 1) && (++times) < 10){
			HZQXInsurranceUtil.logInsurranceOrderMessage("No insurrance key", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return key;
	}
	public static int getPartnerId() {
		int times = 0;
		while(partnerId < 1 && (++times) < 10){
			HZQXInsurranceUtil.logInsurranceOrderMessage("No insurrance partnerId", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return partnerId;
	}
	public static String getURL() {
		int times = 0;
		while((url == null || url.length() < 1) && (++times) < 10){
			HZQXInsurranceUtil.logInsurranceOrderMessage("No insurrance url", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return url;
	}
	public static void setPartnerId(String partnerId) {
		HZQXInsurranceUtil.partnerId = IdUtil.convertToInteger(partnerId, 0);
	}
	public static void setKey(String key) {
		HZQXInsurranceUtil.key = key;
	}
	public static void setURL(String url) {
		HZQXInsurranceUtil.url = url;
	}
	
	/*************
	 * 创建每次调用的流水号
	 * @return
	 */
	public static final String createTransactionNo(){
		final String time = TimeUtil.formatCurrentTime(TimeFormat.YYYYMMDDHHMMSS);
		final String randStr = String.valueOf(RandomUtils.nextInt(99999) + 100000).substring(1);
		return TRAN_NO_PREFIX_NORMAL + time + randStr;
	}
	/**********
	 * 请求成功
	 * @param respCode
	 * @return
	 */
	public static final boolean isReponseSuc(final int respCode){
		return respCode  == 0;
	}
	
	/*************************************************惠泽齐欣平台的接口********************************
	 * 查询上架的产品信息列表
	 * 接口名： productList
	 * 渠道商可通过此接口查询所有对接产品
	 */
	private static final List<Product> queryAllOnShelfProductList()
	{
		final ProductListReq req = new ProductListReq();
		req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
		req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
		final CommonResult<ProductListResp> resp = operation.productList(req);
		if(!isReponseSuc(resp.getRespCode())){
			logger.fatal("tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
			return null;
		}
		final List<Product> products = resp.getData().getProducts();
		if(CollectionUtils.isEmpty(products)){
			logger.fatal("tran-no:" + req.getTransNo() + ", No products, why?");
			return new ArrayList<Product>(0);
		}
		//过滤特定的保险编号
		final List<String> filterCaseCode = new ArrayList<String>();
		filterCaseCode.add("QX000000127471");
		filterCaseCode.add("QX000000127470");
		filterCaseCode.add("QX000000127468");
		filterCaseCode.add("QX000000127469");
		//过滤下架的那些产品
		final List<Product> result = new LinkedList<Product>();
		for(final Product p : products){
			if(p.getOffShelf() == 1 || filterCaseCode.contains(p.getCaseCode())){//是否下架 0：否 1：是
				continue;
			}
			result.add(p);
		}
		return result;
	}	
	
	public static final InsurranceProductDetail getProduct(final String caseCode){
		if(caseCode == null || caseCode.length() < 1){
			return null;
		}
		return productDetailMap.get(caseCode);
	}
	
	public static final List<InsurranceProductDetail> getAllProducts(){
		return allProductDetails;
	}
	
	public static final List<InsurranceCategory> getAllCategories(){
		return allCategories;
	}
	
	public static final List<InsurranceCompany> getAllCompanies(){
		return allCompanies;
	}
	
	/*************
	 * 刷选保险产品
	 * @param company
	 * @param type
	 * @return
	 */
	public static final List<InsurranceProductDetail> allProductsByCategory(final long categoryId){
		final List<InsurranceProductDetail> result = new LinkedList<InsurranceProductDetail>();
		for(final InsurranceProductDetail p : allProductDetails){
			if(p.getSecondCategory() == categoryId){
				result.add(p);
			}
		}
		return result;
	}
	
	public static final List<InsurranceProductDetail> allProductsByCompany(final String companyCnName){
		final List<InsurranceProductDetail> result = new LinkedList<InsurranceProductDetail>();
		for(final InsurranceProductDetail p : allProductDetails){
			if(p.getCompanyCnName().equals(companyCnName)){
				result.add(p);
			}
		}
		return result;
	}
	
	/*************
	 * 根据产品方案代码查询产品投保属性信息，包含投保参数、校验规则以及约束信息
	 * 需要下单时进行判断
	 * @param caseCode
	 * @return
	 */
	public static final InsureAttribute getProductInsureAttribute(final String caseCode)
	{
		InsureAttribute attr = productInsureAttributeMap.get(caseCode);
		if(attr != null){
			return attr;
		}
		try{
			final ProductInsureAttrReq req = new ProductInsureAttrReq();
			req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
			req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
			req.setCaseCode(caseCode);
			final CommonResult<ProductInsureAttrResp> resp = operation.productInsuredAttr(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
				return null;
			}
			attr = resp.getData().getInsureAttribute();
//			System.out.println(">>>>>>>" + caseCode);
//			for(final AttributeModule am : attr.getAttrModules()){
//				final StringBuilder sb = new StringBuilder(am.getModuleId() + "=" + am.getName() + "=" + am.getRemark());
//				if(CollectionUtils.isNotEmpty(am.getProductAttributes())){
//					for(final ProductAttribute g : am.getProductAttributes()){
//						sb.append("["+ ReflectionToStringBuilder.toString(g) + "]");
//						if(CollectionUtils.isNotEmpty(g.getAttributeValues())){
//							for(final AttributeValue g2 : g.getAttributeValues()){
//								sb.append("["+ ReflectionToStringBuilder.toString(g2) + "]");
//							}
//						}
//					}
//				}
//				System.out.println(sb.toString() + "\n<<<<<<<<<");
//			}
			productInsureAttributeMap.put(caseCode, attr);
			return attr;
		}catch(Exception ex){
			logger.fatal("fail to queryProductInsureAttribute， err:" + ex.getMessage(), ex);
			return null;
		}
	}
	
	/*************
	 * 获取用户填写信息的开关属性
	 * @param caseCode
	 * @return
	 */
	public static final ProductInsureAttributeSetting getProductInsureAttributeSetting(final String caseCode)
	{
		ProductInsureAttributeSetting setting = productInsureAttributeSettingMap.get(caseCode);
		if(setting != null){
			return setting;
		}
		setting = new ProductInsureAttributeSetting();;
		final InsureAttribute attr = HZQXInsurranceUtil.getProductInsureAttribute(caseCode);
		if(attr != null)
		{//根据给的保险属性进行设置
			final List<AttributeModule> attrModules = attr.getAttrModules();
			if(CollectionUtils.isEmpty(attrModules)){
				return setting;
			}
			for(final AttributeModule am : attrModules)
			{
				if(CollectionUtils.isEmpty(am.getProductAttributes())){
					continue;
				}
				if(am.getModuleId() == Module.InsurePerson.id//投保人信息
					|| am.getModuleId() == Module.InsuredPerson.id//被保险人信息
					|| am.getModuleId() == Module.Other.id)//其他信息
				{
					for(final ProductAttribute g : am.getProductAttributes())
					{
						HZQXInsurranceUtil.setProductInsureAttribute(g, setting);
					}
				}
			}
			productInsureAttributeSettingMap.put(caseCode, setting);
		}
		return setting;
	}
	
	//根据属性进行开关设置
	private static final void setProductInsureAttribute(final ProductAttribute attr, final ProductInsureAttributeSetting setting){
		if(attr.getRequired() == 0){
			return;
		}
		final String apiName = attr.getApiName().toLowerCase();
		if(apiName.contains("ename")){//是否需要输入英文名字
			setting.setOpenPinyinOrEnglishName(true);
		}else if(apiName.contains("destination")){//是否需要输入出行目的
			setting.setOpenTripDestination(true);
		}else if(apiName.contains("visacity")){//是否需要签证目的地城市
			setting.setOpenVisaProcessingCity(true);
		}else if(apiName.contains("relationid")){//是否需要投保和被投保的关系
			setting.setOpenPersonalRelation(true);
		}else if(apiName.contains("cardtype"))
		{//证件类型
			if(CollectionUtils.isEmpty(attr.getAttributeValues())){
				return;
			}
			for(final AttributeValue av: attr.getAttributeValues())				
			{
				if(av.getValue().contains("其他") || av.getValue().contains("其它")){
					setting.setOpenHongKongMacauLaissezPasser(true);//是否允许港澳通行证
					setting.setOpenArmyCard(true);//是否允许军人证
					setting.setOpenMainlandTravelPermit(true);//是否允许台胞回乡证
				}/*else if(av.getValue().contains("港澳通行")){//和我们的不一致
					setting.setOpenHongKongMacauLaissezPasser(true);//是否允许港澳通行证
				}*/else if(av.getValue().contains("军官")){
					setting.setOpenArmyCard(true);//是否允许军官证
				}else if(av.getValue().contains("台胞")){
					setting.setOpenMainlandTravelPermit(true);//是否允许台胞回乡证
				} 
			}
		}
	}
	
	/************
	 * 试算接口名： simpleTrial, 获取原价, 单位元
	 * 根据被保人生日获取指定保障期限内承保价格
	 */
	private static final SimpleTrialResp doOrderTrail(final InsurranceProductDetail product, final long birthday,
			final long startDate, final long endDate, final int tryTimes)
	
	{
		final CommonResult<SimpleTrialResp> resp = HZQXInsurranceUtil.doOrderTrailProxy(product, birthday,
				startDate, endDate, tryTimes);
		if(resp == null || !isReponseSuc(resp.getRespCode())){
			return null;
		}
		return resp.getData();
	}
	
	//查询价格 获取最原始的信息
	private static final CommonResult<SimpleTrialResp> doOrderTrailProxy(final InsurranceProductDetail product, final long birthday,
			final long startDate, final long endDate, final int tryTimes)
	{
		if(tryTimes < 1){
			return null;
		}
		try{
			final SimpleTrialReq req = new SimpleTrialReq();
			req.setBirthday(TimeUtil.formatDate(birthday, TimeFormat.yyyy_MM_dd));
			req.setCaseCode(product.getCaseCode());
			//12.1.-12.2，一天的话是这种 投保日期是按照0点计算的
			req.setEndDate(TimeUtil.formatDate(endDate+Miliseconds.OneDay.miliseconds, TimeFormat.yyyy_MM_dd)); 
			req.setStartDate(TimeUtil.formatDate(startDate, TimeFormat.yyyy_MM_dd));
			req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
			req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
			final Map<String, String> extMap = new HashMap<String, String>();
			//承保年龄
			final Calendar cal = Calendar.getInstance();
			if(product.getRestrictGeneInsurantDate() != null && product.getRestrictGeneInsurantDate().getValues() != null
					&& product.getRestrictGeneInsurantDate().getValues().size() > 1)
			{
				//min和max中间的都是可以保的，根据type来判断，如果type是1的话是具体值，如果type为2，就根据step来递增选项，只要在min和max中间的都可以投保						
				final RestrictDictionary rd = HZQXInsurranceUtil.getRestrictGeneInsurantDateByAge(product.getRestrictGeneInsurantDate(), 
						TimeUtil.calculateAge(birthday));
				if(rd != null){
					extMap.put(product.getRestrictGeneInsurantDate().getName(), rd.getValue() + rd.getUnit());
				}
			}		
			//保障期限要加上试算因子
			if(product.getRestrictGeneInsurantDateLimit() != null && product.getRestrictGeneInsurantDateLimit().getValues() != null
					&& product.getRestrictGeneInsurantDateLimit().getValues().size() > 1)
			{
				//min和max中间的都是可以保的，根据type来判断，如果type是1的话是具体值，如果type为2，就根据step来递增选项，只要在min和max中间的都可以投保						
				final int days = (int)((endDate - startDate) / Miliseconds.OneDay.miliseconds) + 1;					
				final RestrictDictionary rd = HZQXInsurranceUtil.getRestrictGeneInsurantDateLimit(product.getRestrictGeneInsurantDateLimit(), days);
				if(rd != null){
					if("2".equals(rd.getType())){//如果type是2的话，就需要传具体的天数过来
						if(rd.getUnit().contains("天")){//天
							extMap.put(product.getRestrictGeneInsurantDateLimit().getName(), days + rd.getUnit());							
						}else{
							cal.setTimeInMillis(startDate);
							final int startYear = cal.get(Calendar.YEAR);
							final int startMonth = cal.get(Calendar.MONTH);
							cal.setTimeInMillis(endDate);
							if(rd.getUnit().contains("年")){//年
								extMap.put(product.getRestrictGeneInsurantDateLimit().getName(),
										MathUtil.max(1, cal.get(Calendar.YEAR) - startYear) + rd.getUnit());
							}else{//月
								extMap.put(product.getRestrictGeneInsurantDateLimit().getName(),
										MathUtil.max(1, (cal.get(Calendar.YEAR) - startYear)*12
												+ cal.get(Calendar.MONTH) - startMonth) + rd.getUnit());
							}
						}					
					}else{
						extMap.put(product.getRestrictGeneInsurantDateLimit().getName(), rd.getValue() + rd.getUnit());
					}
				}		
			}				
			if(extMap.size() > 0){
				req.setExtMap(extMap);
			}
			final CommonResult<SimpleTrialResp> resp = operation.simpleTrial(req);
			if(resp == null || !isReponseSuc(resp.getRespCode())){
				logger.fatal("doOrderTrailProxy==> tran-no:" + req.getTransNo() + ", resp:" 
						+ ReflectionToStringBuilder.toString(resp));
			}
			return resp;
		}catch(Exception ex){
			logger.fatal("对不起，试算接口出错了， ex=" + ex.getMessage(), ex);
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			return doOrderTrailProxy(product, birthday, startDate, endDate, tryTimes-1);
		}
	}
	
	/*************
	 * 根据出行时间， 获取对应的保障时间的时间段
	 * min和max中间的都是可以保的，根据type来判断，如果type是1的话是具体值，如果type为2，就根据step来递增选项，只要在min和max中间的都可以投保		
	 * @param insurantDateLimit
	 * @param days
	 * @return
	 */
	public static final RestrictDictionary getRestrictGeneInsurantDateLimit(final RestrictGene insurantDateLimit, final int days)
	{
		for(final RestrictDictionary rd : insurantDateLimit.getValues())
		{			
			if(rd.getUnit().contains("年")){
				if(days >= rd.getMin()*365 && days <= rd.getMax()*366){
					return rd;
				}
			}else if(rd.getUnit().contains("月")){
				if(days >= rd.getMin()*28 && days <= rd.getMax()*31){
					return rd;
				}
			}else if(rd.getUnit().contains("天")){
				if(days >= rd.getMin() && days <= rd.getMax()){
					return rd;
				}
			}
		}	
		return null;
	}
	
	/*************
	 * 根年龄， 获取对应的承保年龄段
	 * min和max中间的都是可以保的，根据type来判断，如果type是1的话是具体值，如果type为2，就根据step来递增选项，只要在min和max中间的都可以投保		
	 * @param insurantDate
	 * @return
	 */
	public static final RestrictDictionary getRestrictGeneInsurantDateByAge(final RestrictGene insurantDate, final int age)
	{
		for(final RestrictDictionary rd : insurantDate.getValues())
		{
			if(age >= rd.getMin() && age <= rd.getMax()){
				return rd;
			}
		}
		return null;
	}
	
	/************
	 * 接口名： download
	 * 渠道商可通过此接口下载保单详细信息至本地
	 * 返回下载地址
	 * @param insureNum
	 * @return
	 */
	public static final String downloadInsurranceOrder(final String insureNum, final int tryTimes) 
	{
		if(tryTimes < 1){
			return null;
		}
		final PolicyUrlReq req = new PolicyUrlReq();
		req.setInsureNum(insureNum);
		req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
		req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
		try{
			final CommonResult<PolicyUrlResp> resp = operation.downloadUrl(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
				return null;
			}
			final String url = resp.getData().getFileUrl();
			logger.fatal("downloadInsurranceOrder : insureName=" + insureNum + ", url=" + url);			
			return url;
		}catch(Exception ex){
			if(tryTimes < 1){
				return null;
			}
			logger.error("fail to downloadInsurranceOrder, ex="+ex.getMessage(), ex);
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			return downloadInsurranceOrder(insureNum, tryTimes-1);
		}
	}
	
	/***********
	 * 接口名：productInsuredArea
	 * 获取产品可投保省市地区（二级和三级地区需根据上一级地区编码再次调用此接口获得）
	 * @param caseCode
	 * @param areaCode
	 * @return
	 */
	public static final List<AreaInfo> queryInsuredAreas(final String caseCode, final String areaCode)
	{
		List<AreaInfo> list = productInsuredAreaMap.get(caseCode + "-" + areaCode);
		if(list != null){
			return list;
		}
		final ProductInsuredAreaReq req = new ProductInsuredAreaReq();
		req.setCaseCode(caseCode);
		req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
		req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
		req.setAreaCode(areaCode == null ? "" : areaCode);
		try{
			final CommonResult<ProductInsuredAreaResp> resp = operation.productInsuredArea(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
				return null;
			}
			list = resp.getData().getAreas();
			productInsuredAreaMap.put(caseCode + "-" + areaCode, list);
		}catch(Exception ex){
			logger.fatal("fail to querytInsuredAreas, ex=" + ex.getMessage(), ex);
		}
		return list;
	}
	
	/***********
	 * 接口名：propertyAddress
	 * 用于获取财产地址信息（二级和三级地区需根据上一级地区编码再次调用此接口获得）
	 * @param caseCode
	 * @param areaCode
	 * @return
	 */
	public static final List<AreaInfo> querytPropertyAddress(final String caseCode, final String areaCode)
	{
		List<AreaInfo> list = productPropertyAddressMap.get(caseCode + "-" + areaCode);
		if(list != null){
			return list;
		}
		final PropertyAddressReq req = new PropertyAddressReq();
		req.setCaseCode(caseCode);
		req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
		req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
		req.setAreaCode(areaCode == null ? "" : areaCode);
		try{
			final CommonResult<PropertyAddressResp> resp = operation.propertyAddress(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
				return null;
			}
			list = resp.getData().getAreas();
			productPropertyAddressMap.put(caseCode + "-" + areaCode, list);
		}catch(Exception ex){
			logger.fatal("fail to querytPropertyAddress, ex=" + ex.getMessage(), ex);
		}
		return list;
	}
	
	
	/***********
	 * 接口名：：productInsuredJob
	 * 获取产品职业信息
	 */
	public static final List<ProductInsuredJob> queryInsuredJobs(final String caseCode)
	{
		List<ProductInsuredJob> list = productInsuredJobMap.get(caseCode);
		if(list != null){
			return list;
		}
		final ProductInsuredJobReq req = new ProductInsuredJobReq();
		req.setCaseCode(caseCode);
		req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
		req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
		try{
			final CommonResult<ProductInsuredJobResp> resp = operation.productInsuredJob(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
				return null;
			}
			list = resp.getData().getJobs();
			productInsuredJobMap.put(caseCode, list);
		}catch(Exception ex){
			logger.fatal("fail to queryInsuredJobs, ex=" + ex.getMessage(), ex);
		}
		return list;
	}
	
	/***********
	 * 接口名：：productDestination 境外旅意险获取支持出行目的地国家
	 */
	public static final List<ProductDestination> queryInsuredDestinations(final String caseCode)
	{
		List<ProductDestination> list = productInsuredDestinationMap.get(caseCode);
		if(list != null){
			return list;
		}
		final ProductDestinationReq req = new ProductDestinationReq();
		req.setCaseCode(caseCode);
		req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
		req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
		try{
			final CommonResult<ProductDestinationResp> resp = operation.productDestination(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
				return null;
			}
			list = resp.getData().getDestinations();
			productInsuredDestinationMap.put(caseCode, list);
		}catch(Exception ex){
			logger.fatal("fail to queryInsuredJDestinations, ex=" + ex.getMessage(), ex);
		}
		return list;
	}
	
	
	/***********
	 * 接口名：orderApply
	 * 渠道商将客户的投保信息用json格式通过参数提交给慧择网进行承保，慧择网将承保数据结果异步返回给渠道商，渠道商根据响应结果进行相应（成功/失败）处理。
	 * 渠道商需提供回调地址供慧择网返回数据结果，具体流程参考出单通知接口
	 * OrderApplyResp
	 * transNo	String	交易流水号，与请求报文的流水号一致
	 * partnerId	String	渠道商身份标识，由慧择指定
	 * insureNum	String	投保单号
	 * @param caseCode：方案代码
	 * @param orderId：我们这边的订单id
	 * @param applicationData: 投保时间等数据 applicationData.setApplicationDate("2015-08-05 09:40:00"); 
	 * 			applicationData.setStartDate("2016-12-10"); applicationData.setEndDate("2016-12-19");
	 * @param applicantInfo: 投保人信息
	 * @param insurantInfo: 被保人信息
	 */
	public static final CommonResult<InsureResp> submitOrder2QXSystem(final SasInsurranceOrder order,
			final List<SasInsurranceOrderApplier> appliers, final int tryTimes) 
	{
		final InsurranceProductDetail product = productDetailMap.get(order.getCaseCode());
		if(product == null || tryTimes < 1){
			return null;
		}
		try{
			final InsureReq req = new InsureReq();
			req.setTransNo(TRAN_NO_PREFIX_ORDER + order.getId() + "-" + tryTimes);
			req.setPartnerId(HZQXInsurranceUtil.partnerId);
			req.setCaseCode(order.getCaseCode());	
			req.setStartDate(TimeUtil.formatDate(order.getStartDate(), TimeFormat.yyyy_MM_dd));
			//12.1.-12.2，一天的话是这种 投保日期是按照0点计算的
			req.setEndDate(TimeUtil.formatDate(order.getEndDate()+Miliseconds.OneDay.miliseconds, TimeFormat.yyyy_MM_dd));
			
			//投保人信息	
			final Applicant applicant = new Applicant(); 
			//投保人信息
			applicant.setcName(order.getInsureUserName());
			applicant.seteName(order.getInsureUserEnglishName());
			applicant.setCardType(HZInsurranceConfigUtil.getIdentityCardType(order.getInsureUserIdentityCardType()));
			applicant.setCardCode(order.getInsureUserIdentityCardNum()); //投保人证件号
			applicant.setSex(SexType.Female.type == order.getInsureUserSex() ? 0 : 1); //投保人性别 0：女 1：男
			applicant.setBirthday(TimeUtil.formatDate(order.getInsureUserBirthday(), TimeFormat.yyyy_MM_dd)); //投保人出生日期 格式：yyyy-MM-dd
			applicant.setMobile(order.getInsureUserPhone());
			applicant.setEmail(order.getInsureUserEmail());
			applicant.setApplicantType(0); //投保人类型 0：个人（默认） 1：公司
			req.setApplicant(applicant);
			
			//被保人信息列表	
			final List<Insurant> insurantInfos = new LinkedList<Insurant>(); 
			for(final SasInsurranceOrderApplier applier : appliers)
			{
				final Insurant insurantInfo = new Insurant();
				insurantInfo.setInsurantId(String.valueOf(applier.getId()));
				insurantInfo.setcName(applier.getUserName());
				insurantInfo.seteName(applier.getEnglishUserName());//英文名
				insurantInfo.setSex(SexType.Female.type == applier.getSex() ? 0 : 1); //投保人性别 0：女 1：男
				insurantInfo.setCardType(HZInsurranceConfigUtil.getIdentityCardType(applier.getIdentityCardType()));
				insurantInfo.setCardCode(applier.getIdentityCardNum());
				insurantInfo.setBirthday(TimeUtil.formatDate(applier.getBirthday(), TimeFormat.yyyy_MM_dd));
				insurantInfo.setMobile(applier.getPhone());
				if(StringUtils.isNotBlank(insurantInfo.getCardCode())
						&& HtmlUtil.isStringEqualOREmpty(insurantInfo.getCardCode(), applicant.getCardCode(), true)){
					insurantInfo.setRelationId(PersonalRelation.Self.type); //关系
				}else{
					insurantInfo.setRelationId(applier.getPersonalRelation()); //关系
				}
				insurantInfo.setCount(1);
				//必填	产品单价（单位：分）
				insurantInfo.setSinglePrice(applier.getAmount().multiply(new BigDecimal(100)).longValue());	
				
				insurantInfos.add(insurantInfo);
			}
			//添加保险人信息
			req.setInsurants(insurantInfos);
			//req.setOrderNo(String.valueOf(order.getId()));
			
			//其他信息
			final OtherInfo otherInfo = new OtherInfo();
			otherInfo.setVisaCity(order.getVisaProcessingCity()); //签证城市
			otherInfo.setTripPurpose(order.getTripPurpose());//出行目的地
			otherInfo.setDestination(order.getTripDestination()); //旅行目的地
			req.setOtherInfo(otherInfo);
			final CommonResult<InsureResp> resp = operation.simpleInsure(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + req.getTransNo() + ", msg=" + resp.getRespMsg()
						+ ", resp:" + ReflectionToStringBuilder.toString(resp));
			}
			return resp;
		}catch(Exception ex){
			logger.fatal("fail to sumitOrder2QXSystem:" + ex.getMessage(), ex);
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			return submitOrder2QXSystem(order, appliers, tryTimes-1);
		}
    }

	/*************
	 * 本地支付
	 * @param order
	 * @param tryTimes
	 * @return
	 */
	public static final BinaryEntry<Boolean, String> payInsurranceOrder(final SasInsurranceOrder order,  
			final List<SasInsurranceOrderApplier> appliers, final boolean isTestEnvironment,
			final int tryTimes)
	{
		if(tryTimes < 1){
			return new BinaryEntry<Boolean, String>(false, "到达最大重试次数");
		}
		try{
			final LocalPayReq req = new LocalPayReq();
			req.setInsureNums(order.getHzInsureNum());
			//渠道总价
			if(isTestEnvironment){
				req.setMoney(1);
			}else{
				req.setMoney(order.getTotalAmount().subtract(order.getSaiHuiTongCommission()).multiply(new BigDecimal(100)).longValue());
			}
			req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
			req.setTransNo(TRAN_NO_PREFIX_ORDER_PAY + order.getId() + "-" + tryTimes);
			final CommonResult<LocalPayResp> resp = operation.localPay(req);
			if(isReponseSuc(resp.getRespCode()) && resp.getData() != null && StringUtils.isNotBlank(resp.getData().getInsureNums())){
				return new BinaryEntry<Boolean, String>(true, null);
			}else{
				logger.fatal("Fail to payInsurranceOrder=>tran-no:" + req.getTransNo()
						 + ", req:" + ReflectionToStringBuilder.toString(req)
						 + ", resp:" + ReflectionToStringBuilder.toString(resp));
				return new BinaryEntry<Boolean, String>(false, resp.getRespMsg());
			}
		}catch(Exception ex){
			logger.fatal("fail to request2CancelInsurranceOrder, error:" + ex.getMessage(), ex);
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			return payInsurranceOrder(order, appliers, isTestEnvironment, tryTimes-1);
		}
	}
	
	/****************
	 * 接口名：surrenderPolicy 、 对未起保（未生效）的保单进行退保处理 
	 * @param order
	 * @param appliers
	 * @param tryTimes
	 * @return
	 */
	public static final boolean cancelInsurranceOrder(final SasInsurranceOrder order, 
			final List<SasInsurranceOrderApplier> appliers, final int tryTimes) 
	{
		if(tryTimes < 1){
			return false;
		}
		try{
			final SurrenderPolicyReq req = new SurrenderPolicyReq();
			req.setInsureNum(order.getHzInsureNum());
			req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
			req.setTransNo(TRAN_NO_PREFIX_ORDER_REFUND + order.getId() + "-" + tryTimes);
			final CommonResult<SurrenderPolicyResp> resp = operation.surrenderPolicy(req);
			if(isReponseSuc(resp.getRespCode()) && resp.getData() != null && StringUtils.isNotBlank(resp.getData().getInsureNum())){
				return true;
			}else{
				logger.fatal("Fail to request2CancelInsurranceOrder=>tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
				return false;
			}
		}catch(Exception ex){
			logger.fatal("fail to request2CancelInsurranceOrder, error:" + ex.getMessage(), ex);
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			return cancelInsurranceOrder(order, appliers, tryTimes-1);
		}
	}
	
	/********
	 * 可查看投保单详细信息接口名： insureDetail
	 * 如果账户类型为0，查询结果包含投保人信息，被保险人信息，保单明细等信息
	 */
	public static final OrderDetail queryOrderDetail(final SasInsurranceOrder order, final int tryTimes) 
	{
		if(tryTimes < 1){
			return null;
		}
		try{
			final OrderDetailReq req = new OrderDetailReq();
			req.setInsureNum(order.getHzInsureNum());
			req.setPartnerId(HZQXInsurranceUtil.getPartnerId());
			req.setTransNo(HZQXInsurranceUtil.createTransactionNo());
			final CommonResult<OrderDetailResp> resp = operation.orderDetail(req);
			if(!isReponseSuc(resp.getRespCode())){
				logger.fatal("tran-no:" + req.getTransNo() + ", resp:" + ReflectionToStringBuilder.toString(resp));
				ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
				return queryOrderDetail(order, tryTimes-1);
			}
			return resp.getData().getOrderDetail();
		}catch(Exception ex){
			ThreadUtil.sleepNoException(Miliseconds.TwoSeconds.miliseconds);
			return queryOrderDetail(order, tryTimes-1);
		}
	}

	/***********************************************************自行添加的辅助函数***************************************************
	 * 投保的开始时间 - 起止之间的天数 是否 是在允许的投保时间范围内
	 * @param startDate
	 * @param endDate
	 * @param insureDateRanges
	 * @return
	 */
	public static final boolean isInsureStartEndDateRangeValidOfInsureDateLimit(final InsurranceProductDetail detail,
			long startDate, long endDate)
	{
		if(detail == null){
			return false;
		}
		if(startDate >= (TimeUtil.getTodayStartTime().getTimeInMillis() + Miliseconds.OneDay.miliseconds * detail.getFirstDate())){
			startDate = TimeUtil.getOneDayStartTime(startDate).getTimeInMillis(); //将时分秒初始化为0
			endDate = TimeUtil.getOneDayStartTime(endDate).getTimeInMillis(); //将时分秒初始化为0
			final int days = (int)((endDate - startDate) / Miliseconds.OneDay.miliseconds) + 1;//因为包含头和尾， 所以需要加1天
			return HZQXInsurranceUtil.getRestrictGeneInsurantDateLimit(detail.getRestrictGeneInsurantDateLimit(), (int)days) != null;
		}
		return false;
	}
	
	/****************
	 * 是否生日是在某一个投保年龄段之内的，是则 返回true
	 * @param birthday
	 * @param now
	 * @param insureAgeRanges
	 * @return
	 */
	public static final boolean isBirthdayValidOfAgeRange(final InsurranceProductDetail detail,final long birthday)
	{
		if(detail == null){
			return false;
		}
		return HZQXInsurranceUtil.getRestrictGeneInsurantDateByAge(detail.getRestrictGeneInsurantDate(), 
				TimeUtil.calculateAge(birthday)) != null;
	}
	
	public static final boolean isBirthdaysValidOfAgeRange(final InsurranceProductDetail detail, final long[] birthdays)
	{
		if(detail == null){
			return false;
		}
		//计算年龄		
		for(final long birthday : birthdays)
		{			
			if(HZQXInsurranceUtil.getRestrictGeneInsurantDateByAge(detail.getRestrictGeneInsurantDate(), 
					TimeUtil.calculateAge(birthday)) == null){
				return false;
			}
		}
		return true;
	}

	/**************
	 * 生成订单code
	 * @param order
	 * @return
	 */
	public static final String generateOrderCode(final SasInsurranceOrder order){
		return order.getCaseCode() + "#" + order.getId();
	}
	/**************
	 * 其他偶外字段的开关控制，例如
	 * 1. 拼音/英文名必填;
	 * 2. 出行目的、出行目的地必填
	 * 如果没有填写， 则返回错误信息
	 * @param otherApplierFieldSwitch
	 * @param applier
	 * @return
	 */
	public static final String getErrorByCheckOtherApplierNecessaryField(final InsurranceProductDetail detail, final SasInsureUser insureUser, 
			final String tripDestination, final String visaProcessingCity)
	{
		if(insureUser == null){
			return null;
		}
		final ProductInsureAttributeSetting setting = HZQXInsurranceUtil.getProductInsureAttributeSetting(detail.getCaseCode());
		//"被保人证件类型限制判断
		final Set<IdentityCardType> allowedIdCardTypeSet = new HashSet<IdentityCardType>();
		allowedIdCardTypeSet.add(IdentityCardType.IdCard);
		allowedIdCardTypeSet.add(IdentityCardType.Passport);
		final StringBuilder allowedIdCardTypeString = new StringBuilder("该保险要求投保人的证件必须为身份证，护照");
		if(setting.isOpenArmyCard()){
			allowedIdCardTypeSet.add(IdentityCardType.ArmyCard);
			allowedIdCardTypeString.append("，军官证");
		}
		if(setting.isOpenHongKongMacauLaissezPasser()){
			allowedIdCardTypeSet.add(IdentityCardType.HongKongMacauLaissezPasser);
			allowedIdCardTypeString.append("，港澳通行证");
		}
		if(setting.isOpenMainlandTravelPermit()){
			allowedIdCardTypeSet.add(IdentityCardType.MainlandTravelPermit);
			allowedIdCardTypeString.append("，台胞证");
		}
		if(!allowedIdCardTypeSet.contains(IdentityCardType.parse(insureUser.getIdentityCardType()))){
			return allowedIdCardTypeString.toString() + "！";
		}
		//"拼音/英文名必填"
		if(setting.isOpenPinyinOrEnglishName()){
			if(StringUtils.isBlank(insureUser.getEnglishUserName())){
				return "该保险要求投保人必须输入英文名或姓名拼音！";
			}					
		}
		//"出行目的地"
		if(setting.isOpenTripDestination()){
			if(StringUtils.isBlank(tripDestination)){
				return "该保险要求输入出行目的地！";
			}
		}
		//"签证办理城市"
		if(setting.isOpenVisaProcessingCity()){
			if(StringUtils.isBlank(visaProcessingCity)){
				return "该保险要求输入签证办理城市！";
			}
		}
		return null;
	}
	
	public static final String getErrorByCheckOtherApplierNecessaryField(final InsurranceProductDetail detail, 
			final SasInsurranceOrderApplier applier)
	{
		if(applier == null){
			return null;
		}

		final ProductInsureAttributeSetting setting = HZQXInsurranceUtil.getProductInsureAttributeSetting(detail.getCaseCode());
		//"被保人证件类型限制判断
		final Set<IdentityCardType> allowedIdCardTypeSet = new HashSet<IdentityCardType>();
		allowedIdCardTypeSet.add(IdentityCardType.IdCard);
		allowedIdCardTypeSet.add(IdentityCardType.Passport);
		final StringBuilder allowedIdCardTypeString = new StringBuilder("该保险要求投保人的证件必须为身份证，护照");
		if(setting.isOpenArmyCard()){
			allowedIdCardTypeSet.add(IdentityCardType.ArmyCard);
			allowedIdCardTypeString.append("，军官证");
		}
		if(setting.isOpenHongKongMacauLaissezPasser()){
			allowedIdCardTypeSet.add(IdentityCardType.HongKongMacauLaissezPasser);
			allowedIdCardTypeString.append("，港澳通行证");
		}
		if(setting.isOpenMainlandTravelPermit()){
			allowedIdCardTypeSet.add(IdentityCardType.MainlandTravelPermit);
			allowedIdCardTypeString.append("，台胞证");
		}
		if(!allowedIdCardTypeSet.contains(IdentityCardType.parse(applier.getIdentityCardType()))){
			return allowedIdCardTypeString.toString() + "！";
		}
		//"拼音/英文名必填"
		if(setting.isOpenPinyinOrEnglishName()){
			if(StringUtils.isBlank(applier.getEnglishUserName())){
				return "该保险要求被保险人必须输入英文名或姓名拼音！";
			}					
		}
		//"投保人必须是被保人的：妻子、丈夫、儿子、女儿、父亲、母亲"
		//if(setting.isOpenPersonalRelation()){
		//	if(applier.getPersonalRelation() == PersonalRelation.Other.type 
		//			|| applier.getPersonalRelation() == PersonalRelation.Self.type){
		//		return "该保险要求投保人必须是被保人的：妻子、丈夫、儿子、女儿、父亲、母亲！";
		//	}
		//}
		return null;
	}
	
	/********
	 * 算法：我们利润的一半给俱乐部， 5/5分成
	 * 0.5 + 0.5 * 赛会通折扣
	 * @param saihuitongDiscountRatio
	 * @return
	 */
	public static final BigDecimal getClubDiscountRatio(final BigDecimal saihuitongDiscountRatio){
		return saihuitongDiscountRatio.add(new BigDecimal(1)).divide(new BigDecimal(2), 2, BigDecimal.ROUND_HALF_UP);
	}

	
	public static final void logInsurranceOrderMessage(final String state, final long orderId, final long transactionId, final String desc){
		if(desc == null){
			logger.error("[Insure-" + state + "][o#" + orderId + "][t#" + transactionId + "]" );
		}else{
			logger.error("[Insure-" + state + "][o#" + orderId + "][t#" + transactionId + "][" + desc + "]" );
		}
	}
	
	
	public static void main(String[] args) throws Exception{
//		final ObjectMapper objectMapper = new ObjectMapper();
//		Configure.Channel.partnerId = HZQXInsurranceUtil.getPartnerId();
//		Configure.Channel.channelKey = HZQXInsurranceUtil.getKey();
//		System.out.println(objectMapper.writeValueAsString(queryInsurranceOrder("0000050268603022")));
//		for(final String v : new String[]{"1-45天", "1天", "1年", "3-180天", "1-1年", "365天以内", "2-364天以内"}){
//			System.out.print(v + "=");
//			System.out.println(parseDateRange(v));
//		}
//		for(final String v : new String[]{"30天-80周岁", "0-80周岁", "1-17周岁", "0-17周岁"}){
//			System.out.print(v + "=");
//			System.out.println(parseAgeRange(v));
//		}
//		String json = null;
//		try {
//			json = IOUtil.readTextFromHttpURL(null, Encoding.UTF8);
//		} catch (Exception e) {
//			logger.fatal("fail to createOrderCallback, ex=" + e.getMessage() + ", json=" + json, e);
//		}
//		logger.fatal("createOrderCallback, json=" + json);
//		final HZAsyncOrderInfo callback = JsonUtil.getObject(json, HZAsyncOrderInfo.class);
//		if(callback == null){
//			System.out.println( "failure" );
//		}
		Calendar cal = Calendar.getInstance();
		int days = cal.get(Calendar.DAY_OF_YEAR);
		System.out.println(days);
		cal.set(2017, 6, 27, 18, 0, 0);
		days = cal.get(Calendar.DAY_OF_YEAR);
		System.out.println(days);
	}
}
