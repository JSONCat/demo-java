package com.sas.core.util.wxpay;

/**
 * User: zhulm
 * Date: 2014/10/22
 * Time: 21:29
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sas.core.util.MD5SignUtil;

/**
 * 请求被扫支付API需要提交的数据
 */
public class WXPayRequestDTO {

	private final String appid = WXPayConstant.getAppID(); //微信分配的公众账号ID（企业号corpid即为此appId）公众账号ID, String(32)
		
	private final String mch_id = WXPayConstant.MchID; //微信支付分配的商户号 商户号  String(32)
	
	private final String device_info = "WEB"; //终端设备号(门店号或收银设备ID)，注意：PC网页或公众号内支付请传"WEB"
	
	private final String nonce_str; //随机字符串，不长于32位。推荐随机数生成算法

	private final String sign; //签名，详见签名生成算法 String(32)

	private final String body; //商品或支付单简要描述 String(32)

	private final String detail; //商品名称明细列表， 可选，String(8192)
	
	private final String attach; //附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据，可选，String(127) 

	private final String out_trade_no; //商户系统内部的订单号,32个字符内、可包含字母, 其他说明见商户订单号  String(32)
	
	private final String fee_type = "CNY"; //符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型

	private final String total_fee; //订单总金额，只能为整数，详见支付金额，接口中参数支付金额单位为【分】

	private final String spbill_create_ip; //APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。

	private final String time_start; //订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则

	private final String time_expire; //订单失效时间，格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010。其他详见时间规则	注意：最短失效时间间隔必须大于5分钟

	//private final String goods_tag; //商品标记，代金券或立减优惠功能的参数，说明详见代金券或立减优惠

	private final String notify_url; //接收微信支付异步通知回调地址

	private final String trade_type; //取值如下：JSAPI，NATIVE，APP，WAP,详细说明见参数规定

	private final String product_id; //trade_type=NATIVE，此参数必传。此id为二维码中包含的商品ID，商户自行定义。

	private final String openid; //可选，trade_type=JSAPI，此参数必传，用户在商户appid下的唯一标识。下单前需要调用【网页授权获取用户信息】接口获取到用户的Openid。企业号请使用【企业号OAuth2.0接口】获取企业号内成员userid，再调用【企业号userid转openid接口】进行转换。

    public WXPayRequestDTO(String body, String detail, String attach, String out_trade_no,
			String total_fee, String spbill_create_ip, String time_start,
			String time_expire, String notify_url, String trade_type,
			String product_id, String openid) 
    {
		this.body = body;
		this.detail = detail;
		this.attach = attach;
		this.out_trade_no = out_trade_no;
		this.total_fee = total_fee;
		this.spbill_create_ip = spbill_create_ip;
		this.time_start = time_start;
		this.time_expire = time_expire;
		this.notify_url = notify_url;
		this.trade_type = trade_type;
		this.product_id = product_id;
		this.openid = openid;

        //随机字符串，不长于32 位
        this.nonce_str = MD5SignUtil.generateRandomKey("abcdefghijklmnopqrstuvwxyz0123456789", 32);

        //根据API给的签名规则进行签名
        this.sign =  WXSignature.getSign(toMapForSignature()); //把签名数据设置到Sign这个属性中
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
        if(StringUtils.isNotBlank(detail)){
            map.put("detail", detail);
        }
        if(StringUtils.isNotBlank(attach)){
            map.put("attach", attach);
        }
        if(StringUtils.isNotBlank(out_trade_no)){
            map.put("out_trade_no", out_trade_no);
        }
        if(StringUtils.isNotBlank(fee_type)){
            map.put("fee_type", fee_type);
        }
        if(StringUtils.isNotBlank(total_fee)){
            map.put("total_fee", total_fee);
        }
        if(StringUtils.isNotBlank(spbill_create_ip)){
            map.put("spbill_create_ip", spbill_create_ip);
        }
        if(StringUtils.isNotBlank(time_start)){
            map.put("time_start", time_start);
        }
        if(StringUtils.isNotBlank(time_expire)){
            map.put("time_expire", time_expire);
        }
        if(StringUtils.isNotBlank(notify_url)){
            map.put("notify_url", notify_url);
        }
        if(StringUtils.isNotBlank(trade_type)){
            map.put("trade_type", trade_type);
        }
        if(StringUtils.isNotBlank(product_id)){
            map.put("product_id", product_id);
        }
        if(StringUtils.isNotBlank(openid)){
            map.put("openid", openid);
        }
        return map;
    }

	public String getAppid() {
		return appid;
	}

	public String getMch_id() {
		return mch_id;
	}

	public String getDevice_info() {
		return device_info;
	}

	public String getNonce_str() {
		return nonce_str;
	}

	public String getSign() {
		return sign;
	}

	public String getBody() {
		return body;
	}

	public String getDetail() {
		return detail;
	}

	public String getAttach() {
		return attach;
	}

	public String getOut_trade_no() {
		return out_trade_no;
	}

	public String getFee_type() {
		return fee_type;
	}

	public String getTotal_fee() {
		return total_fee;
	}

	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}

	public String getTime_start() {
		return time_start;
	}

	public String getTime_expire() {
		return time_expire;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public String getTrade_type() {
		return trade_type;
	}

	public String getProduct_id() {
		return product_id;
	}

	public String getOpenid() {
		return openid;
	}
}