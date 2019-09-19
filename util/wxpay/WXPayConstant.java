package com.sas.core.util.wxpay;

import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.util.ThreadUtil;
import com.sas.core.util.TransactionUtil;

/**
 * User: rizenguo Date: 2014/10/29 Time: 14:40 这里放置各种配置数据
 * 微信支付相关的常量
 */
public class WXPayConstant {
	
	public static final String ErrorPayImage = "http://img.saihuitong.com/system/errorpay.jpg";

	public static final String WxPayHTTPProtocol = "http://"; //"https://"
	
	// sdk的版本号
	public static final String sdkVersion = "java sdk 1.0.1";

	// 这个就是自己要保管好的私有Key了（切记只能放在自己的后台代码里，不能放在任何可能被看到源代码的客户端程序中）
	// 每次自己Post数据给API的时候都要用这个key来对所有字段进行签名，生成的签名会放在Sign这个字段，API收到Post数据的时候也会用同样的签名算法对Post过来的数据进行签名和验证
	// 收到API的返回的时候也要用这个key来对返回的数据算下签名，跟API的Sign数据进行比较，如果值不一致，有可能数据被第三方给篡改

	//注意：需要把加密用的可以设置成和普通微信公众号支付一样， 不一样的话很多地方都得改，详见WXPayConstant.key
	private static String key = ""; 
	public static final void setKey(String k){
		key = k;
	}
	public static final String getKey(){
		while(key.length() < 1){
			TransactionUtil.logOrderMessage("No weixin pay Key", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return key;
	}
	
	//公众号信息
	public static final String PublicAccountName = "activitypay"; //公众号名字
	
	private static String PublicAccountId = ""; //微信支付：赛事活动支付
	
	public static final void setPublicAccountId(String k){
		PublicAccountId = k;
	}
	public static final String getPublicAccountId(){
		while(PublicAccountId.length() < 1){
			TransactionUtil.logOrderMessage("No weixin pay PublicAccountId", 0, 0, "not initialzied, sleep!");
			ThreadUtil.sleepNoException(Miliseconds.FiveSeconds.miliseconds);
		}
		return PublicAccountId;
	}
	
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
	public static final String MchID = "1294862701"; 

	//HTTPS证书密码，默认密码等于商户号MCHID
	public static final String CertPassword = MchID;

	// 受理模式下给子商户分配的子商户号
	public static final String subMchID = MchID;

	// 是否使用异步线程的方式来上报API测速，默认为异步模式
	private static boolean useThreadToDoReport = true;

	// 机器IP
	private static String ip = "";

	// 以下是几个API的路径：
	// 1）被扫支付API
	public static String PAY_API = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	// 2）被扫支付查询API
	public static String PAY_QUERY_API = "https://api.mch.weixin.qq.com/pay/orderquery";

	// 3）退款API
	public static String REFUND_API = "https://api.mch.weixin.qq.com/secapi/pay/refund";

	// 4）退款查询API
	public static String REFUND_QUERY_API = "https://api.mch.weixin.qq.com/pay/refundquery";

	// 5）撤销API
	public static String REVERSE_API = "https://api.mch.weixin.qq.com/secapi/pay/reverse";

	// 6）下载对账单API
	public static String DOWNLOAD_BILL_API = "https://api.mch.weixin.qq.com/pay/downloadbill";

	// 7) 统计上报API
	public static String REPORT_API = "https://api.mch.weixin.qq.com/payitil/report";

	// 8) 关闭订单API
	public static String CLOSE_API = "https://api.mch.weixin.qq.com/pay/closeorder";

	public static boolean isUseThreadToDoReport() {
		return useThreadToDoReport;
	}

	public static void setUseThreadToDoReport(boolean useThreadToDoReport) {
		WXPayConstant.useThreadToDoReport = useThreadToDoReport;
	}
	public static void setIp(String ip) {
		WXPayConstant.ip = ip;
	}

	public static String getIP() {
		return ip;
	}
	
	public static enum TradeType{
		JSAPI("JSAPI"),
    NATIVE("NATIVE"),
    APP("APP"),
    MWEB("MWEB");
		public String type;
		private TradeType(final String _type){
		  this.type = _type;
    }
    
	}

}
