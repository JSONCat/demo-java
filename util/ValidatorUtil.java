package com.sas.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.sas.core.constant.UserConstant.SexType;

/**
 * 基本的验证类型
 * @author zlm
 */
public class ValidatorUtil {
	
	// 邮箱正则表达式
	protected static final String  EMAIL_REGEX 			= "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[\\-\\.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
	
	// 中国大陆手机号码
	protected static final String  MOBILE_REGEX	 		= "^1\\d{10}$";
	public	   static final String  MOBILE_ERR_MSG		= "手机号码不合法";
	
	// 密码正则表达式
	protected static final String  PASSWD_REGEX 		= "^[a-zA-Z0-9`~!@#$%^&*()+=|{}':;\"',\\[\\]\\.\\-<>/?]{6,20}$";
	public	   static final String  PASSWD_ERR_MSG		= "密码仅支持字母、数字或者英文符号，6-20位，区分大小写";

	// 域名正则表达式
	protected static final String  SUBDOMAIN_REGEX 		= "^[A-Za-z0-9|-]{4,20}$";
	public	   static final String SUBDOMAIN_ERR_MSG    = "域名输入错误，仅支持字母a-z、数字0-9或者连字符-，4-20位，不区分大小写";
	
	// 昵称正则表达式
	protected static final String  NICKNAME_REGEX 		= "^[\u4E00-\u9FA5A-Za-z0-9_\\.]{1,20}$";
	public 	   static final String  NICKNAME_ERR_MSG 	= "昵称仅支持中英文、数字和下划线，1-20位，区分大小写";
	
	// 姓名正则表达式
	protected static final String  REALNAME_REGEX 		= "^[\u4E00-\u9FA5A-Za-z]{1,10}$";
	public    static final String  REALNAME_ERR_MSG 	= "姓名仅支持中英文(1-10位)";
	
	// 证件号码验证
	protected static final String  ID_CARD_REGEXP 		= "^\\d{6}(18|19|20)?\\d{2}(0[1-9]|1[012])(0[1-9]|[12]\\d|3[01])\\d{3}(\\d|X)$";
	public    static final String  ID_CARD_ERR_MSG 	    = "证件号码不合法";
	
	protected static final String	CODE_REGEXP			= "[0-9a-zA-Z]{1,10}";
	public    static final String	CODE_ERR_MSG 	    = "代码不合法";
	
	protected static final String  PASSPORT_REGEXP      = "^(?![a-zA-Z]+$)[\\da-zA-Z]{6,18}$";
	public    static final String  PASSPORT_ERR_MSG 	= "护照不合法";
	
	//域名正则表达式
	protected static final String  URL_REGEXP    = "^([hH][tT]{2}[pP]:/*|[hH][tT]{2}[pP][sS]:/*|[fF][tT][pP]:/*)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+(\\?{0,1}(([A-Za-z0-9-~]+\\={0,1})([A-Za-z0-9-~]*)\\&{0,1})*)$";
	public  static final  String   URL_ERR_MSG  = "网址不合法";
	
	// 评论的最大长度
	protected static final int MAX_COMMENT_LENGTH = 3000;
	// 备注的最大长度
	protected static final int MAX_REMARK_LENGTH = 140;
	//倒计时标题最大长度
	public static final int MAX_COUNTDOWN_TITLE_LENGTH = 5;
	
	//全角和半角字符
	public static final Map<String, String> digitAndLetterChineseMap = new HashMap<String, String>();
	static{
		final String[] replaceChars = new String[]{"．０１２３４５６７８９ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ",
			".0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"};
		for (int i = 0; i < replaceChars[0].length(); i++) { 
			digitAndLetterChineseMap.put(String.valueOf(replaceChars[0].charAt(i)), String.valueOf(replaceChars[1].charAt(i)));
		}
	}
	
	/**
	 * 邮箱验证规则
	 */
	public static boolean emailValidate(String email) {
		if(StringUtils.isBlank(email)){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(EMAIL_REGEX);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	
	/**
	 * 手机号码验证规则
	 * 
	 * @param cellPhone
	 * @return
	 */
	public static boolean mobileValidate(String mobile) {
		if(StringUtils.isBlank(mobile)){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(MOBILE_REGEX);
			Matcher matcher = regex.matcher(mobile);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 护照验证规则
	 * 
	 * @param cellPhone
	 * @return
	 */
	public static boolean passportValidate(String code) {
		if(StringUtils.isBlank(code)){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(PASSPORT_REGEXP);
			Matcher matcher = regex.matcher(code);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	  /**
   * url地址验证
   *
   * @param url
   * @return
   */
	public static boolean urlValidate(String url){
	  if(StringUtils.isBlank(url)){
			return false;
		}
		boolean flag = false;
	  try {
			Pattern regex = Pattern.compile(URL_REGEXP);
			Matcher matcher = regex.matcher(url);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
  }
	
	/***********
	 * 判断手机或者座机的合法性
	 * @param telephone
	 * @return
	 */
	public static boolean mobileORTelephoneValidate(String telephone){
		if(StringUtils.isBlank(telephone)){
			return false;
		}
		for(final char ch : telephone.toCharArray())
		{
			if(ch == ' ' || ch == '+' || ch == '-' || ch == ' ' || ch == '－'  || ch == '＋' 
					|| (ch >= '0' && ch <= '9')
					|| (ch >= '０' && ch <= '９')){
				continue;
			}
			return false;
		}
		return true;
	}
	
	public static final String parseValidPhoneLetters(final String telephone)
	{
		if(StringUtils.isBlank(telephone)){
			return "";
		}
		final StringBuilder letters = new StringBuilder("");
		for(final char ch : telephone.toCharArray())
		{
			if(ch == '+' || ch == '-'|| (ch >= '0' && ch <= '9')){
				letters.append(ch);
			}else if(ch == '－'){
				letters.append('-');
			}else if(ch == '＋' ){
				letters.append('+');
			}else{
				switch(ch)
				{
					case '０': letters.append('0'); break;
					case '１': letters.append('1'); break;
					case '２': letters.append('2'); break;
					case '３': letters.append('3'); break;
					case '４': letters.append('4'); break;
					case '５': letters.append('5'); break;
					case '６': letters.append('6'); break;
					case '７': letters.append('7'); break;
					case '８': letters.append('8'); break;
					case '９': letters.append('9'); break;
				}
			}
		}
		return letters.toString();
	}
	
	/**
	 * 密码验证规则
	 * 
	 * <p>
	 * 	字母、数字或者英文符号，6-20位，区分大小写
	 * </p>
	 * 
	 * @param pwd 密码
	 * @return
	 */
	public static boolean passwordValidate(String pwd) {
		if(StringUtils.isBlank(pwd)){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(PASSWD_REGEX);
			Matcher matcher = regex.matcher(pwd);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	
	/**
	 * 昵称验证规则
	 * 
	 * <p>
	 *   中英文、数字和下划线，2-20位，区分大小写
	 * </p>
	 * 
	 * @param pwd 密码
	 * @return
	 */
	public static boolean nicknameValidate(String nickname) {
		if(nickname == null){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(NICKNAME_REGEX);
			Matcher matcher = regex.matcher(nickname);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	
	/**
	 * 真实姓名验证
	 * 
	 * <p>
	 *   中英文、2-20位，区分大小写
	 * </p>
	 * 
	 * @param pwd 密码
	 * @return
	 */
	public static boolean realNameValidate(String realName) {
		if(realName == null){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(REALNAME_REGEX);
			Matcher matcher = regex.matcher(realName);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	
	//性别验证
	public static boolean sexValidate(char sex){
		if(sex == SexType.Female.type){
			return true;
		}else if(sex == SexType.Male.type){
			return true;
		}else if(sex == SexType.Unknown.type){
			return true;
		}
		return false;
	}
	
	
	/**
	 * String类型的数字参数验证
	 * @param domain
	 * @return
	 */
	public static boolean strDigitalValidate(String digitalStr) {
		boolean flag = false;
		String check = "^\\d+$";
		try {
			if(digitalStr != null){
				Pattern regex = Pattern.compile(check);
				Matcher matcher = regex.matcher(digitalStr.trim());
				flag = matcher.matches();
			}
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 验证身份证号的合法性
     * @param param required 身份证号
     * @param birthday 需要比对的生日 选填
     * @param gender 需要比对的性别 选填
     * 校验位规则     公式:∑(ai×Wi)(mod 11)……………………………………(1)
     *  公式(1)中：
     * i----表示号码字符从由至左包括校验码在内的位置序号；
     * ai----表示第i位置上的号码字符值；
     * Wi----示第i位置上的加权因子，其数值依据公式Wi=2^(n-1）(mod 11)计算得出。
     * i 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1
     * Wi 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2 1
     * 详见前端identityCodeValid.js
	 * @param cardNo
	 * @return
	 */
	public static boolean cardNoValidate(String cardNo) {
		if(cardNo != null){
			cardNo = cardNo.trim();
		}
		if(cardNo == null || (cardNo.length() != 15 && cardNo.length() != 18)){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(ID_CARD_REGEXP);
			cardNo = cardNo == null ? "" : StringUtils.trim(cardNo);
			Matcher matcher = regex.matcher(cardNo);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		if(flag){
			//判断地址信息
			//var city={11:"北京",12:"天津",13:"河北",14:"山西",15:"内蒙古",21:"辽宁",22:"吉林",23:"黑龙江 ",31:"上海",32:"江苏",33:"浙江",34:"安徽",35:"福建",36:"江西",37:"山东",41:"河南",42:"湖北 ",43:"湖南",44:"广东",45:"广西",46:"海南",50:"重庆",51:"四川",52:"贵州",53:"云南",54:"西藏 ",61:"陕西",62:"甘肃",63:"青海",64:"宁夏",65:"新疆",71:"台湾",81:"香港",82:"澳门",91:"国外 "};
			final int addressType = IdUtil.convertToInteger(cardNo.substring(0, 2), 0);
			if(addressType == 11 || addressType == 12 || addressType == 13 || addressType == 14 || addressType == 15 
					|| addressType == 21 || addressType == 22 || addressType == 23 || addressType == 31 || addressType == 32 
					|| addressType == 33 || addressType == 34 || addressType == 35 || addressType == 36 || addressType == 37 
					|| addressType == 41 || addressType == 42 || addressType == 43 || addressType == 44 || addressType == 45
					|| addressType == 46 || addressType == 50 || addressType == 51 || addressType == 52 || addressType == 53
					|| addressType == 54 || addressType == 61 || addressType == 62 || addressType == 63 || addressType == 64 
					|| addressType == 65 || addressType == 71 || addressType == 81 || addressType == 82 || addressType == 91)
			{
				if(cardNo.length() < 18){
					return true;
				}
				//18位身份证需要验证最后一位校验位
				//∑(ai×Wi)(mod 11)
                //加权因子
                final int[] factor = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                //校验位
                long sum = 0;
                for (int i = 0; i < 17; i++) {
                    sum += (cardNo.charAt(i)-'0') * factor[i];
                }
                final char last = "10X98765432".charAt((int)(sum % 11));
                return last == cardNo.charAt(17);
			}
			return false;
		}
		return false;
	}
	
	
	
	/**
	 * 客户端传递的时间验证，至少大于系统当前时间
	 * @param time
	 * @return 合法返回true，否则false
	 */
	public static boolean clientTimeMillisValidate(long time) {
		return System.currentTimeMillis() < time;
	}
	
	/**
	 * 是否是分销商的信息
	 * 
	 * @param  url
	 * @return 合法返回true，否则false
	 */
	public static boolean ResaleShopIdValidate(final String url){
		if(StringUtils.isBlank(url)){
			return false;
		}
		try {
			Pattern regex = Pattern.compile("^shop[0-9]{1,100}\\..+$");
			Matcher matcher = regex.matcher(url);
			return matcher.matches();
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * url 合法性验证
	 * 
	 * @param  url
	 * @return 合法返回true，否则false
	 */
	public static boolean URLValidate(String url){
		if(StringUtils.isBlank(url)){
			return false;
		}
		
		final UrlValidator urlValidator = new UrlValidator();
		return urlValidator.isValid(RequestUtil.removeInValidURLChars(url));
	}
	
	public static void main(String[] args){
		System.out.println(ValidatorUtil.cardNoValidate("41132619840517234x"));
	}
	
	/**
	 * 域名验证规则
	 * 
	 * <p>
	 * 	域名仅支持字母a-z、数字0-9或者连字符-，4-15位，不区分大小写
	 * </p>
	 * 
	 * @param  密码
	 * @return
	 */
	public static boolean subDomainValidate(String domain) {
		if(StringUtils.isBlank(domain)){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(SUBDOMAIN_REGEX);
			Matcher matcher = regex.matcher(domain);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	
	/**
	 * 一级域名合法性验证
	 * 
	 * @param domain
	 * @return
	 */
	public static boolean topLevelDomainValidate(String domain) {
		if(StringUtils.isBlank(domain)){
			return false;
		}
		Pattern p = null;  
	    Matcher m = null;  
	    boolean b = false;  
	    String str = null;  
	    String reg = "^\\w+((-\\w+)|(\\.\\w+))*[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z]+$" ;  
	  
	    p = Pattern.compile(reg);  
	    m = p.matcher(domain);  
	    b = m.matches();  
	          
	    if(!b){  
	       return false;  
	    }  
	          
	    str = m.group();      
	  
	        reg = "^[\\u4e00-\\u9fa5]+$";//纯汉字必须大于1位  
	        p = Pattern.compile(reg);  
	        m = p.matcher(domain);  
	        b = m.matches();  
	        if(b){  
	            String chinese = m.group();   
	            if(chinese.length()<2||chinese.length()>20){  
	                return false;  
	            }  
	        }else{  
	            //判断punycode长度  
	            if(str.length()<3){  
	                return false;  
	            }  
	              
	            //如果第一位、二位不是中文，就判断第三、四位是否是“-”  
	            String str1 = str.substring(0,3);;  
	            String reg1  ="^[-a-zA-Z0-9]*$";  
	            p = Pattern.compile(reg1);  
	            m = p.matcher(str1);  
	            b = m.matches();  
	  
	            if(b){  
	                if(str.indexOf("-")==2||str.indexOf("-")==3){             
	                    return false;  
	                }  
	            }  
	            //长度为63位  
//	          str = Punycode.encode(str);//转换成punycode 不用转化  
//	          if(str.length()>59){  
//	              return "您输入的域名过长!";  
//	          }  
	              
	            //判断输入的域名是否超长  
	            int valueLength = 0;  
	            String chinese = "[\u4e00-\u9fa5]";    
	            for(int i=0;i<str.length();i++){  
	                String temp = str.substring(i,i+1);  
	                if(temp.matches(chinese)){  
	                    valueLength += 2;  
	                }else{  
	                    valueLength++;  
	                }  
	            }  
	            if(valueLength>63){  
	                return false;  
	            }  
	        }  
	        return true;
	}
	
	public static boolean codeValidate(String code) {
		if(code == null){
			return false;
		}
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile(CODE_REGEXP);
			Matcher matcher = regex.matcher(code);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
}