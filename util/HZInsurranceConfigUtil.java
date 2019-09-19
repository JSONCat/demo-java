/**
 * 
 */
package com.sas.core.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.sas.core.constant.UserConstant.IdentityCardType;

/**
 * 惠泽保险的配置UTIL
 * @author Administrator
 *
 */
public class HZInsurranceConfigUtil {	
	
	/**********
	 * 由于系统升级， 部分老订单不支持下载保单
	 * @return
	 */
	public static final long getMaxPayTimeSupportDownload()
	{
		return TimeUtil.getMiliseconds(2017, 6, 27, 18, 0, 0);
	}
	
	/************
	 * 获取身份类型， 转成惠泽需要的类型
	 * @param identityCardType
	 * @return
	 */
	public static final int getIdentityCardType(final char identityCardType)
	{
		if(IdentityCardType.IdCard.type == identityCardType){
			return 1;	//身份证
		}else if(IdentityCardType.Passport.type == identityCardType){
			return 2;	//护照
		}/*else if(IdentityCardType.HongKongMacauLaissezPasser.type == identityCardType){
			return 5;	//港澳通行证
		}else if(IdentityCardType.MainlandTravelPermit.type == identityCardType){
			return 7;	//台胞证护照
		}else if(IdentityCardType.ArmyCard.type == identityCardType){
			return 6;	//军官证
		}*/else{
			return 99;	//其他
		}
//		1	身份证
//		2	护照
//		3	出生证
//		4	驾照
//		5	港澳通行证
//		6	军官证
//		7	台胞证
//		8	警官证
//		99	其他
	}
		
	
	/**********
	 * 计算赛会通的折扣
	 * 安心户外	76折
	 * 越玩越野	76折
	 * 自行车保障	7折
	 * 综合户外赛事保险	76折
	 * 任性游	76折
	 * “游乐美”高额休闲游	6.8折
	 * 领队责任“户外无忧”保障	76折
	 * 366户外年险	76折
	 * 齐欣乐游全球	6折
	 */
	public static final BigDecimal createSaihuitongDiscountRatioByProductName(final String productName){
		if((productName.indexOf('骑') >= 0 && productName.indexOf('行') >= 0)
				|| productName.indexOf("自行车") >= 0){
			return new BigDecimal(0.7);
		}else if(productName.indexOf('游') >= 0 && productName.indexOf('乐') >= 0 && productName.indexOf('美') >= 0){
			return new BigDecimal(0.68);
		}else if(productName.contains("齐欣") && productName.indexOf('乐') >= 0 && productName.indexOf('游') >= 0){
			return new BigDecimal(0.6);
		}else if(productName.indexOf('齐') >= 0 && productName.indexOf('乐') >= 0 && productName.indexOf('游') >= 0){
			return new BigDecimal(0.7);
		}else if(productName.indexOf("定向活动") >= 0){
			return new BigDecimal(0.81);
		}
		return new BigDecimal(0.76);
	}
	
	/*******
	 * 5.	产品分类
	 */
	public static final String parseCategoryName(final int type)
	{
		switch(type)
		{
			case 2000:	return "意外保险";
			case 2018:	return "人身意外保险";
			case 2016:	return "交通意外保险";
			case 2017:	return "航空意外保险";
			case 2001:	return "旅游保险";
			case 2019:	return "申根签证保险";
			case 2020:	return "境外旅行保险";
			case 2021:	return "国内旅游保险";
			case 2022:	return "出国留学保险";
			case 2041:	return "户外运动保险";
			case 2008:	return "家财保险";
			case 2034:	return "自住型家财险";
			case 2035:	return "出租型家财险";
			case 2036:	return "承租型家财险";
			case 2037:	return "网店专用型家财险";
			case 2002:	return "健康保险";
			case 2027:	return "重大疾病保险";
			case 2028:	return "住院医疗保险";
			case 2025:	return "护理保险";
			case 2026:	return "疾病保险";
			case 2006:	return "人寿保险";
			case 2029:	return "定期寿险";
			case 2030:	return "终身寿险";
			case 2032:	return "两全保险";
			default: return "其他";
		}
	}
	
	/*************
	 * 解析拒赔条款
	 * @param types
	 * @return
	 */
	public static final String parseExclusionLiability(final int[] types)
	{
		if(ArrayUtils.isEmpty(types)){
			return "";
		}
		final StringBuilder sb = new StringBuilder("");
		for(final int t : types)
		{
			if(t == 1){
				sb.append(sb.length() > 0 ? "，故意伤害" : "故意伤害");
			}else if(t == 2){
				sb.append(sb.length() > 0 ? "，违法犯罪" : "违法犯罪"); 
			}else if(t == 3){
				sb.append(sb.length() > 0 ? "，自杀" : "自杀"); 
			}else if(t == 4){
				sb.append(sb.length() > 0 ? "，酒驾事故" : "酒驾事故");
			}else if(t == 5){
				sb.append(sb.length() > 0 ? "，吸食毒品" : "吸食毒品"); 
			}else if(t == 6){
				sb.append(sb.length() > 0 ? "，战争死亡" : "战争死亡"); 
			}else if(t == 7){
				sb.append(sb.length() > 0 ? "，先天遗传" : "先天遗传");
			}else if(t == 8){
				sb.append(sb.length() > 0 ? "，艾滋病死亡" : "艾滋病死亡");
			}
		}
		return sb.toString();
	}
	
	/*********
	 * 人际关系
	 * @author Administrator
	 */
	private static final Map<Integer, PersonalRelation> personalRelationById = new HashMap<Integer, PersonalRelation>();
	private static final Map<String, PersonalRelation> personalRelationByName = new HashMap<String, PersonalRelation>();
	public static enum PersonalRelation{
		Self(1, "本人"),
		Wife(2, "妻子"),
		Husband(3, "丈夫"),
		Son(4, "儿子"),
		Daughter(5, "女儿"),
		Father(6, "父亲"),
		Mother(7, "母亲"),
		Brother(8,	"兄弟"),
		Sister(9,	"姐妹"),
		p10(10,	"祖父/祖母/外祖父/外祖母"),
		p11(11,	"孙子/孙女/外孙/外孙女"),
		p12(12,	"叔父/伯父/舅舅"),
		p13(13,	"婶/姨/姑"),
		p14(14,	"侄子/侄女/外甥/外甥女"),
		p15(15,	"堂兄弟/堂姐妹/表兄弟/表姐妹"),
		p16(16,	"岳父"),
		p17(17,	"岳母"),
		p18(18,	"同事"),
		p19(19,	"朋友"),
		p20(20,	"雇主"),
		p21(21,	"雇员"),
		p22(22,	"法定监护人"),
		Other(23, "其他");
		public final int type;
		public final String name;
		private PersonalRelation(final int type, final String name){
			this.type = type;
			this.name = name;
			personalRelationById.put(type, this);
			personalRelationByName.put(name, this);
		}
		public static final PersonalRelation parseByType(final int t){
			final PersonalRelation pr = personalRelationById.get(t);
			return pr == null ? Other : pr;
		}
		public static final PersonalRelation parseByType(final String name){
			final PersonalRelation pr = personalRelationByName.get(name);
			return pr == null ? Other : pr;
		}
	}
	
	/******
	 * 出行目的
	 * @author Administrator
	 *
	 */
	public static enum TripPurpose{	
		Travel(1, "旅游"),
		Business(2, "商务"),
		VisitFamily(3, "探亲"),
		Study(4, "留学"),
		Work(5, "务工"),
		Other(6, "其他");
		public final int type;
		public final String name;
		private TripPurpose(final int type, final String name){
			this.type = type;
			this.name = name;
		}
		public static final TripPurpose parseByType(final int t){
			switch(t)
			{
				case 1 : return Travel;
				case 2 : return Business;
				case 3 : return VisitFamily;
				case 4 : return Study;
				case 5 : return Work;
				default : return Other;
			}
		}
		public static final TripPurpose parseByType(final String name){
			if("旅游".equals(name)){
				return Travel;
			}else if("商务".equals(name)){
				return Business;
			}else if("探亲".equals(name)){
				return VisitFamily;
			}else if("留学".equals(name)){
				return Study;
			}else if("务工".equals(name)){
				return Work;
			}else{
				return Other;
			}
		}
	}
	
	/************
	 * 信息模块
	 * @author Administrator
	 *
	 */
	public static enum Module
	{
		InsurreDate(102, "选择起保日期"),
		BeneficiaryInformation(30, "受益人信息"),
		Other(40, "其他信息"),
		InsurePerson(10, "投保人信息"),
		InsuredPerson(20, "被保险人信息"),
		EmergencyContact(101, "紧急联系人");
		public final int id;
		public final String name;
		private Module(final int l, final String name){
			this.id = l;
			this.name = name;
		}
		public boolean isMe(final int v){
			return this.id == v;
		}
	}
}
