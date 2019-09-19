 /**
 */
package com.sas.core.util.meta;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.CommonConstant.FieldLengthLimit;
import com.sas.core.constant.EncryptConstant.Md5Salt;
import com.sas.core.constant.FileConstant.ViewFileDir;
import com.sas.core.constant.ThirdPartConstant.QiNiuImageCompress;
import com.sas.core.constant.UserConstant;
import com.sas.core.constant.UserConstant.SexType;
import com.sas.core.constant.UserConstant.UserLevel;
import com.sas.core.dto.FourEntry;
import com.sas.core.dto.UserDetail;
import com.sas.core.meta.SasUser;
import com.sas.core.meta.User;
import com.sas.core.meta.UserExt;
import com.sas.core.util.CollectionUtils;
import com.sas.core.util.HtmlUtil;
import com.sas.core.util.IdUtil;
import com.sas.core.util.MD5SignUtil;
import com.sas.core.util.PinyinUtil;
import com.sas.core.util.QiNiuUtil;
import com.sas.core.util.ValidatorUtil;
import com.sas.core.util.XSSUtil;

/**
 * 用户util
 * @author zhuliming
 *
 */
public final class UserUtil {
	
	public static final int USER_NICKNAME_MAX_LENGTH = 20;
	public static final int USER_EMAIL_MAX_LENGTH = 50;
	public static final int USER_IDENTITY_MAX_LENGTH = 18;

	private static final Logger logger = Logger.getLogger(UserUtil.class);
	
	/********************
	 * 生成
	 * @param passwordText
	 * @return
	 */
	public static final String generatePassword(final String passwordText){
		return MD5SignUtil.generateSignature(Md5Salt.UserPassword, passwordText,
				FieldLengthLimit.MD5UserPassword.max);
	}
	
	/************
	 * 获取用户的昵称
	 * @param user
	 * @return
	 */
	public static final String getNickname(final User user){
		if(StringUtils.isNotBlank(user.getNickname())){
			return user.getNickname();
		}
		if(StringUtils.isNotBlank(user.getPhone())){
			return UserUtil.hiddenMobile(user.getPhone());
		}else{
			final int index = (user.getEmail() == null) ? -1 : user.getEmail().indexOf('@');
			return (index > 0) ? user.getEmail().substring(0, index) : user.getEmail();
		}
	}
	
	public static final boolean isUserNicknameVaild(final String nickname){
		if(StringUtils.isNotBlank(nickname) && nickname.length() <= USER_EMAIL_MAX_LENGTH){
			return true;
		}
		return false;
	}
	
	/*************
	 * 过滤昵称非法字符
	 * @param s
	 * @return
	 */
	public static final String filterNickNameValidChars(final String s)
	{
		final StringBuilder sb = new StringBuilder("");
		for(final char ch : s.toCharArray())
		{
			if(HtmlUtil.isChineseOrLetterDigit(ch)){
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	
	/****************
	 * 按照优先级生成昵称
	 * @param nickname
	 * @param trueName
	 * @param mobile
	 * @return
	 */
	public static final String generateOrderContactName(final String trueName, final String nickname, final String mobile)
	{
		if(StringUtils.isBlank(trueName)){
			if(StringUtils.isBlank(nickname)){
				return UserUtil.hiddenMobile(mobile);
			}
			return nickname;
		}
		return trueName;
	}
	
	/***********
	 * 隐藏手机号中间4位
	 * @param mobile
	 * @return
	 */
	public static final String hiddenMobile(final String mobile){
		if(mobile == null || mobile.length() <= 3){
			return mobile;
		}
		final String result = mobile.substring(0, 3) + "****";
		if(mobile.length() <= 7){
			return result;
		}
		return result + mobile.substring(7);
	}

	public static final String hiddenUserName(final String userName){
		if(userName == null || userName.length() <= 1){
			return userName;
		}
		final int length = userName.length();
		if(length < 3){
			return userName.charAt(0) + "*";
		}else if(length < 4){
			return userName.charAt(0) + "*" + userName.charAt(2);
		}else{
			return userName.charAt(0) + "**" + userName.substring(3);
		}
	}
	
	/***************
	 * 隐藏身份证件的前面和中间几位
	 * @param identityCode
	 * @return
	 */
	public static final String hiddenIdentityCode(final String identityCode){
		if(identityCode == null || identityCode.length() <= 8){
			return identityCode;
		}
		String result =identityCode.substring(0, 8);
		if(identityCode.length() <= 14){
			return result + identityCode.substring(8);
		}
		return result + "******" + identityCode.substring(14);
	}
	
	public static final String hiddenQQ(final String qq){
		if(qq == null || qq.length() <= 4){
			return qq;
		}
		String result = qq.substring(0, 4) + "****";
		if(qq.length() <= 8){
			return result ;
		}
		return result + qq.substring(8);
	}
	
	/**
	 * @Title: isUserEmailVaild
	 * @Description: 判断用户邮箱是否正确
	 * @param email
	 * @return
	 * @throws
	 */
	public static final boolean isUserEmailVaild(final String email){
		if(StringUtils.isNotBlank(email) && ValidatorUtil.emailValidate(email)
				&& email.length() <= USER_EMAIL_MAX_LENGTH){
			return true;
		}
		return false;
	}
	
	/**
	 * @Title: isUserPhoneValid
	 * @Description: 判断用户手机是否正确
	 * @param phone
	 * @return
	 * @throws
	 */
	public static final boolean isUserPhoneValid(final String phone){
		if(StringUtils.isNotBlank(phone) && ValidatorUtil.mobileValidate(phone)){
			return true;
		}
		return false;
	}
	
	/**
	 * @Title: hidePassword
	 * @Description: 隐藏用户密码
	 * @param userDetailPageData
	 * @return
	 * @throws
	 */
	public final static List<UserDetail> hidePassword(List<UserDetail> userDetailList){
		if(CollectionUtils.isEmpty(userDetailList)){
			return new ArrayList<UserDetail>(0);
		}
		for(UserDetail userDetail : userDetailList){
			UserUtil.hidePassword(userDetail.getUser());
		}
		return userDetailList;
	}
	
	public final static void hidePassword(User u){
		if(u == null){
			return;
		}
		u.setPassword("***********");
	}
	
	/**
	 * @Title: 仅显示id，头像和昵称
	 * @return
	 * @throws
	 */
	public final static List<User> convert2SimpleDetail(List<User> userList){
		if(CollectionUtils.isEmpty(userList)){
			return new ArrayList<User>(0);
		}
		for(User u : userList){
			u.setCreateTime(0);
			u.setEmail("");
			u.setLastLoginTime(0);
			u.setPassword("");
			u.setPhone("");
			u.setSourceSasId(0);
		}
		return userList;
	}
	
	/****************
	 * 读取系统头像列表
	 * @return
	 */
	public static final String[] listSystemAvatarFiles()
	{
		final URL classesDirUrl = UserUtil.class.getClassLoader().getResource("/");
		try{
			final File classesDir = new File(URLDecoder.decode(classesDirUrl.getPath(), Encoding.UTF8.type));
			final File webinfoDir = classesDir.getParentFile();
			final File avatarDirs = new File(webinfoDir.getParentFile(), ViewFileDir.DirOfSystemAvatar.relativeSubDirPath);
			if(!avatarDirs.exists() || !avatarDirs.isDirectory()){
				logger.error("Fail to listSystemAvatarFiles, for no avatar directory: template directory = "
							+ webinfoDir.getAbsolutePath());
				return new String[0];
			}
			final File[] files = avatarDirs.listFiles();
			final List<String> result = new LinkedList<String>();
			for(int i=0; i<files.length; i++){
				final String nameLowercase = files[i].getName().toLowerCase();
				if(nameLowercase.endsWith("jpg") || nameLowercase.endsWith("gif")){
					result.add(files[i].getName());
				}
			}
			return result.toArray(new String[result.size()]);
		}catch(Exception ex){
			logger.fatal("Fail to init listSystemAvatarFiles, ex="+ex.getMessage(), ex);
			return new String[0];
		}
	}
	
	/**
	 * 生成跳转到用户主页的URI
	 * @param userId
	 * @return
	 */
	public static final String createUserProfilePageURL(final long userId)
	{
		return "/user?id=" + userId;
	}
	
	/************
	 * 从身份证号码中解析出生日：年月日
	 * @param identityCode
	 * @return
	 */
	public static final FourEntry<Integer, Integer, Integer, SexType> parseBirthdayFromUserIdentityCode(String identityCardCode)
	{
		identityCardCode = identityCardCode == null ? null : identityCardCode.trim();
		if(identityCardCode == null || identityCardCode.length() < 14){
			return null;
		}
		SexType sexType = SexType.Male;
		int year = 0, month = 0, days = 0;
		if(identityCardCode.length() < 17){//按照15位处理
			/********
			 * 15位身份证号码各位的含义: 1-2位省、自治区、直辖市代码； 3-4位地级市、盟、自治州代码； 
			 * 5-6位县、县级市、区代码； 7-12位出生年月日,比如670401代表1967年4月1日,与18位的第一个区别；
			 * 13-15位为顺序号，其中15位男为单数，女为双数； 与18位身份证号的第二个区别：没有最后一位的验证码。
			 */
			year = 1900 + IdUtil.convertToInteger(identityCardCode.substring(6, 8), 0);
			month = IdUtil.convertToInteger(identityCardCode.substring(8, 10), 0);
			days = IdUtil.convertToInteger(identityCardCode.substring(10, 12), 0);
			sexType = (identityCardCode.charAt(14) - '0') % 2 == 0 ? SexType.Female : SexType.Male;
		}else{//按照18位处理
			/*****
			 * 18位身份证号码各位的含义: 1-2位省、自治区、直辖市代码； 3-4位地级市、盟、自治州代码； 
			 * 5-6位县、县级市、区代码； 7-14位出生年月日，比如19670401代表1967年4月1日； 
			 * 15-17位为顺序号，其中17位（倒数第二位）男为单数，女为双数；
			 */
			year = IdUtil.convertToInteger(identityCardCode.substring(6, 10), 0);
			month = IdUtil.convertToInteger(identityCardCode.substring(10, 12), 0);
			days = IdUtil.convertToInteger(identityCardCode.substring(12, 14), 0);
			sexType = (identityCardCode.charAt(16) - '0') % 2 == 0 ? SexType.Female : SexType.Male;
		}
		return new FourEntry<Integer, Integer, Integer, SexType>(year, month, days, sexType);
	}
	
	/***************
	 * 设置兴趣爱好
	 * @param hobby
	 * @param otherHobby
	 * @return
	 */
	public static final String createHobby(final String[] hobby, final boolean hasOtherHobby, String otherHobby)
	{
		if(hasOtherHobby){
			if(StringUtils.isNotBlank(otherHobby)){
				otherHobby = otherHobby.replaceAll(DividerChar.Comma.chars, " ");
			}
			if(StringUtils.isNotBlank(otherHobby)){
				otherHobby = XSSUtil.filter(otherHobby.trim(), true);
			}else{
				otherHobby = DividerChar.SingleWells.chars;
			}
		}else{
			otherHobby = DividerChar.SingleWells.chars;
		}
		final StringBuilder sb = new StringBuilder("");
		if(ArrayUtils.isNotEmpty(hobby)){
			for(final String h : hobby)
			{
				if(StringUtils.isBlank(h)){
					continue;
				}
				if(sb.length() > 0){
					sb.append(DividerChar.Comma.chars + XSSUtil.filter(h.trim(), true));
				}else{
					sb.append(h.trim());
				}
			}
		}
		//添加自定义兴趣
		if(otherHobby.length() > 0){
			if(sb.length() > 0){
				sb.append(DividerChar.Comma.chars + otherHobby);
			}else{
				sb.append(otherHobby);
			}
		}
		return sb.toString();
	}
	
	/********************
	 * 删除手机号中的非法字符
	 * @param phone
	 * @return
	 */
	public static final String processUserPhone(final String phone){
		if(phone != null && phone.length() > 0){
			return HtmlUtil.filterChineseDigitals(phone);
		}
		return phone;
	}
	
	/******************
	 * 创建头像链接
	 * @param avatarUrl
	 * @return
	 */
	public static final String createUserAvatar(final String avatarUrl)
	{
		if(avatarUrl == null || avatarUrl.length() < 1){
			return UserConstant.UserDefaultAvatar;
		}
		return QiNiuUtil.generateQiNiuImgURL(avatarUrl, QiNiuImageCompress.cw100h100.style);
	}
	
	/****************
	 * 判断是否为网站会员
	 * @param user
	 * @return
	 */
	public static boolean isSasMember(final SasUser user)
	{
		if(user.getLevel() == UserLevel.Member.level){//判断会员到期
			final long expireTime = user.getMemberExpireTime();
			return expireTime < 0 || System.currentTimeMillis() < expireTime;
		}
		return false;
	}
	
	
	/*****************
	 * 转换成英文地址
	 * @param activity
	 */
	public static final void convert2EnglishAddress(final UserExt user){
		if(user == null){
			return;
		}
		user.setLivingProvince(PinyinUtil.convertAddress2EnglishPinyin(PinyinUtil.convertChineseCountryName2English(user.getLivingProvince())));
		user.setLivingCity(PinyinUtil.convertAddress2EnglishPinyin(user.getLivingCity()));
		user.setLivingAddress(PinyinUtil.convertAddress2EnglishPinyin(user.getLivingAddress()));
	}
	
	/***************
	 * 将兴趣爱好转成英文
	 * @param user
	 */
	public static final void convert2EnglishHobby(final UserExt user){
		if(user == null){
			return;
		}
		final String[] array = user.getHobby().split(DividerChar.Comma.chars);
		final StringBuilder hobby = new StringBuilder("");
		for(final String h : array){
			if(hobby.length() > 0){
				hobby.append(DividerChar.Comma.chars);
			}
			final String n = UserConstant.hobbyChinese2EnglishMap.get(h);
			if(n != null){
				hobby.append(n);
			}else{
				hobby.append(h);
			}
		}
		user.setHobby(hobby.toString());
	}
	
	/**************
	 * 如果是会员并且过期， 则设置为普通用户
	 * @param sasUser
	 * @return
	 */
	public static final int setUserLevelByMemberExpireTime(final SasUser sasUser)
	{
		if(sasUser == null){
			return UserLevel.Initial.level;
		}
		if(isSasMember(sasUser)){
			return UserLevel.Member.level;
		}
		if(UserLevel.Member.level == sasUser.getLevel()){
			sasUser.setLevel(UserLevel.Initial.level);
		}	
		return UserLevel.Initial.level;
	}
	
	public static void main(String[] args)
	{
		System.out.println(UserUtil.parseBirthdayFromUserIdentityCode("420881198906102115"));
		System.out.println(UserUtil.hiddenUserName("朱立名"));
		System.out.println(UserUtil.hiddenUserName("朱立名名"));
		System.out.println(UserUtil.hiddenUserName("朱立名恩恩名"));
	}
}