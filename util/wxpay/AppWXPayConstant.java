package com.sas.core.util.wxpay;

import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.util.ThreadUtil;
import com.sas.core.util.TransactionUtil;

/**
 * 别宅啦APP的微信支付
 * 注意：需要把加密用的可以设置成和普通微信公众号支付一样， 不一样的话很多地方都得改，详见WXPayConstant.key
 * User: rizenguo Date: 2014/10/29 Time: 14:40 这里放置各种配置数据
 * 微信支付相关的常量
 */
public class AppWXPayConstant {
	
	public static final String ErrorPayImage = "http://img.saihuitong.com/system/errorpay.jpg";

	// sdk的版本号
	public static final String sdkVersion = WXPayConstant.sdkVersion;

	// 这个就是自己要保管好的私有Key了（切记只能放在自己的后台代码里，不能放在任何可能被看到源代码的客户端程序中）
	// 每次自己Post数据给API的时候都要用这个key来对所有字段进行签名，生成的签名会放在Sign这个字段，API收到Post数据的时候也会用同样的签名算法对Post过来的数据进行签名和验证
	// 收到API的返回的时候也要用这个key来对返回的数据算下签名，跟API的Sign数据进行比较，如果值不一致，有可能数据被第三方给篡改
	
	//微信分配的公众号ID（开通公众号之后可以获取到）
	private static String AppID = ""; //微信支付：赛事活动支付
	
	public static final void setAppID(String k){
		AppID = k;
	}
	public static final String getAppID(){
		while(AppID.length() < 1){
			TransactionUtil.logOrderMessage("No weixin pay AppID", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return AppID;
	}
	
	private static String AppSecret = ""; //微信支付：赛事活动支付
	
	public static final void setAppSecret(String k){
		AppSecret = k;
	}
	
	public static final String getAppSecret(){
		while(AppSecret.length() < 1){
			TransactionUtil.logOrderMessage("No weixin pay AppSecret", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return AppSecret;
	}	
	
	//微信支付分配的商户号ID（开通公众号的微信支付功能之后可以获取到）
	public static final String MchID = "1469251902"; 

	//HTTPS证书密码，默认密码等于商户号MCHID
	public static final String CertPassword = MchID;

	// 受理模式下给子商户分配的子商户号
	public final static String subMchID = MchID;

	// 是否使用异步线程的方式来上报API测速，默认为异步模式
	private static boolean useThreadToDoReport = true;

	// 机器IP
	private static String ip = "";
	
	public static boolean isUseThreadToDoReport() {
		return useThreadToDoReport;
	}

	public static void setUseThreadToDoReport(boolean useThreadToDoReport) {
		AppWXPayConstant.useThreadToDoReport = useThreadToDoReport;
	}
	
	public static void setIp(String ip) {
		AppWXPayConstant.ip = ip;
	}
	
	public static String getIP() {
		return ip;
	}
}
