package com.sas.core.util.wxpay.meta;

/**
 * User: rizenguo
 * Date: 2014/10/22
 * Time: 21:29
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sas.core.util.wxpay.RandomStringGenerator;
import com.sas.core.util.wxpay.WXPayConstant;
import com.sas.core.util.wxpay.WXPayConstant.TradeType;
import com.sas.core.util.wxpay.WXSignature;

/**
 * 请求被扫支付API需要提交的数据
 */
public class ScanPayReqData {

	// 每个字段具体的意思请查看API文档
	private String appid = "";//
	private String mch_id = "";//
	private String device_info = "WEB";
	private String nonce_str = "";
	private String sign = "";//
	private String body = "";//
	private String attach = "";//
	private String out_trade_no = "";//
	private int total_fee = 0;//
	private String spbill_create_ip = "";//
	private String time_start = "";//
	private String time_expire = "";//
	private String goods_tag = "";//
	private String notify_url = "";
	private String trade_type = "JSAPI";
	private String openid = "";
	private String scene_info = "";
	
	public ScanPayReqData() {
		
	}

	/**
	 * @param authCode
	 *            这个是扫码终端设备从用户手机上扫取到的支付授权号，这个号是跟用户用来支付的银行卡绑定的，有效期是1分钟
	 * @param body
	 *            要支付的商品的描述信息，用户会在支付成功页面里看到这个信息
	 * @param attach
	 *            支付订单里面可以填的附加数据，API会将提交的这个附加数据原样返回
	 * @param outTradeNo
	 *            商户系统内部的订单号,32个字符内可包含字母, 确保在商户系统唯一
	 * @param totalFee
	 *            订单总金额，单位为“分”，只能整数
	 * @param deviceInfo
	 *            商户自己定义的扫码支付终端设备号，方便追溯这笔交易发生在哪台终端设备上
	 * @param spBillCreateIP
	 *            订单生成的机器IP
	 * @param timeStart
	 *            订单生成时间， 格式为yyyyMMddHHmmss，如2009年12 月25 日9 点10 分10
	 *            秒表示为20091225091010。时区为GMT+8 beijing。该时间取自商户服务器
	 * @param timeExpire
	 *            订单失效时间，格式同上
	 * @param goodsTag
	 *            商品标记，微信平台配置的商品标记，用于优惠券或者满减使用
	 */
	public ScanPayReqData(final String appId, final String mchId, String attach, String outTradeNo, String body, int totalFee, String spBillCreateIP,
			String timeStart, String timeExpire, String goodsTag, String notifyUrl, String trade_type, String openId) {
		this(appId, mchId, attach, outTradeNo, body, totalFee, spBillCreateIP,
				timeStart, timeExpire, goodsTag, notifyUrl, trade_type, openId, WXPayConstant.getKey());
	}
	
	public ScanPayReqData(final String appId, final String mchId, String attach, String outTradeNo, String body, int totalFee, String spBillCreateIP,
			String timeStart, String timeExpire, String goodsTag, String notifyUrl, String trade_type, String openId, String key) {
		super();
		
		// 微信分配的公众号ID（开通公众号之后可以获取到）
		setAppid(appId);
		// 微信支付分配的商户号ID（开通公众号的微信支付功能之后可以获取到）
		setMch_id(mchId);
		
		setDevice_info("deviceInof");
		
		// 要支付的商品的描述信息，用户会在支付成功页面里看到这个信息
		setBody(body);

		// 支付订单里面可以填的附加数据，API会将提交的这个附加数据原样返回，有助于商户自己可以注明该笔消费的具体内容，方便后续的运营和记录
		setAttach(attach);

		// 商户系统内部的订单号,32个字符内可包含字母, 确保在商户系统唯一
		setOut_trade_no(outTradeNo);

		// 订单总金额，单位为“分”，只能整数
		setTotal_fee(totalFee);

		// 订单生成的机器IP
		setSpbill_create_ip(spBillCreateIP);

		// 订单生成时间， 格式为yyyyMMddHHmmss，如2009年12 月25 日9 点10 分10
		// 秒表示为20091225091010。时区为GMT+8 beijing。该时间取自商户服务器
		setTime_start(timeStart);

		// 订单失效时间，格式同上
		setTime_expire(timeExpire);

		// 商品标记，微信平台配置的商品标记，用于优惠券或者满减使用
		setGoods_tag(goodsTag);
		
		setNotify_url(notifyUrl);
		
		setTrade_type(trade_type);
		
		setOpenid(openId);

		// 随机字符串，不长于32 位
		setNonce_str(RandomStringGenerator.getRandomStringByLength(32));

		// 根据API给的签名规则进行签名
		final String sign = WXSignature.getSign(this.toMapForSignature(), key);
		setSign(sign);// 把签名数据设置到Sign这个属性中

	}

	public ScanPayReqData(final String appId, final String mchId, final String outTradeNo, final String body, final int totalFee,
                        final String attach, final String spBillCreateIP, final String timeStart, final String timeExpire,
                        final String goodsTag, final String notifyUrl, final String trade_type, final String scene_info) {
		super();
		
		// 微信分配的公众号ID（开通公众号之后可以获取到）
		setAppid(appId);
		// 微信支付分配的商户号ID（开通公众号的微信支付功能之后可以获取到）
		setMch_id(mchId);
		
		// 要支付的商品的描述信息，用户会在支付成功页面里看到这个信息
		setBody(body);

		// 支付订单里面可以填的附加数据，API会将提交的这个附加数据原样返回，有助于商户自己可以注明该笔消费的具体内容，方便后续的运营和记录
		setAttach(attach);

		// 商户系统内部的订单号,32个字符内可包含字母, 确保在商户系统唯一
		setOut_trade_no(outTradeNo);

		// 订单总金额，单位为“分”，只能整数
		setTotal_fee(totalFee);

		// 订单生成的机器IP
		setSpbill_create_ip(spBillCreateIP);

		// 订单生成时间， 格式为yyyyMMddHHmmss，如2009年12 月25 日9 点10 分10
		// 秒表示为20091225091010。时区为GMT+8 beijing。该时间取自商户服务器
		setTime_start(timeStart);

		// 订单失效时间，格式同上
		setTime_expire(timeExpire);

		// 商品标记，微信平台配置的商品标记，用于优惠券或者满减使用
		setGoods_tag(goodsTag);
		
		setNotify_url(notifyUrl);
		
		setTrade_type(trade_type);
		
		// 随机字符串，不长于32 位
		setNonce_str(RandomStringGenerator.getRandomStringByLength(32));

		setScene_info(scene_info); //场景信息
		// 根据API给的签名规则进行签名
		final String sign = WXSignature.getSign(this.toMapForSignature(), WXPayConstant.getKey());
		setSign(sign);// 把签名数据设置到Sign这个属性中
  
	}
	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getMch_id() {
		return mch_id;
	}

	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}

	public String getDevice_info() {
		return device_info;
	}

	public void setDevice_info(String device_info) {
		this.device_info = device_info;
	}

	public String getNonce_str() {
		return nonce_str;
	}

	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public String getOut_trade_no() {
		return out_trade_no;
	}

	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}

	public int getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(int total_fee) {
		this.total_fee = total_fee;
	}

	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}

	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}

	public String getTime_start() {
		return time_start;
	}

	public void setTime_start(String time_start) {
		this.time_start = time_start;
	}

	public String getTime_expire() {
		return time_expire;
	}

	public void setTime_expire(String time_expire) {
		this.time_expire = time_expire;
	}

	public String getGoods_tag() {
		return goods_tag;
	}

	public void setGoods_tag(String goods_tag) {
		this.goods_tag = goods_tag;
	}

	public String getTrade_type() {
		return trade_type;
	}

	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}
  
  public String getScene_info() {
    return scene_info;
  }
  
  public void setScene_info(String scene_info) {
    this.scene_info = scene_info;
  }
  
  public Map<String, String> toMapForSignature(){
        final Map<String,String> map = new HashMap<String, String>();
        map.put("appid", appid);
        map.put("mch_id", mch_id);
        if(StringUtils.isNotBlank(device_info)){
            map.put("device_info", device_info);
        }
        if(StringUtils.isNotBlank(nonce_str)){
            map.put("nonce_str", nonce_str);
        }
        if(StringUtils.isNotBlank(body)){
            map.put("body", body);
        }
        if(StringUtils.isNotBlank(attach)){
            map.put("attach", attach);
        }
        if(StringUtils.isNotBlank(out_trade_no)){
            map.put("out_trade_no", out_trade_no);
        }
        map.put("total_fee", String.valueOf(total_fee));
        
        if(StringUtils.isNotBlank(spbill_create_ip)){
            map.put("spbill_create_ip", spbill_create_ip);
        }
        if(StringUtils.isNotBlank(time_start)){
            map.put("time_start", time_start);
        }
        if(StringUtils.isNotBlank(time_expire)){
            map.put("time_expire", time_expire);
        }
        if(StringUtils.isNotBlank(goods_tag)){
            map.put("goods_tag", goods_tag);
        }
        if(StringUtils.isNotBlank(notify_url)){
            map.put("notify_url", notify_url);
        }
        if(StringUtils.isNotBlank(trade_type)){
            map.put("trade_type", trade_type);
        }
        if(StringUtils.isNotBlank(openid)){
            map.put("openid", openid);
        }
        if (StringUtils.isNotEmpty(scene_info)){
          map.put("scene_info", scene_info);
        }
        return map;
    }

}