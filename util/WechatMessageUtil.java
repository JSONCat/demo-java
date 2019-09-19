package com.sas.core.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.sas.core.constant.SasWechatNotificationConstant;
import com.sas.core.constant.SasWechatNotificationConstant.AutoContentType;
import com.sas.core.constant.SasWechatNotificationConstant.WechatClickConstant;
import com.sas.core.constant.SasWechatNotificationConstant.WechatSwitchState;
import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.constant.TimeConstant.TimeFormat;
import com.sas.core.meta.SasMenuActivity;
import com.sas.core.meta.SasMenuActivityComment;
import com.sas.core.meta.SasMenuActivityOrder;
import com.sas.core.meta.SasMenuArticle;
import com.sas.core.meta.SasMenuArticleComment;
import com.sas.core.meta.SasMenuBbsForum;
import com.sas.core.meta.SasMenuGood;
import com.sas.core.meta.SasMenuGoodComment;
import com.sas.core.meta.SasMenuGoodOrder;
import com.sas.core.meta.SasMenuGoodOrderItem;
import com.sas.core.meta.SasWechatNotificationPerson;
import com.sas.core.meta.SasWechatNotificationSetting;
import com.sas.core.meta.TemplateData;
import com.sas.core.meta.WxTemplate;
import com.sas.core.util.wechat.meta.WechatImage;
import com.sas.core.util.wechat.meta.WechatMessageReply;
import com.sas.core.util.wechat.meta.WechatReplyImage;
import com.sas.core.util.wechat.meta.WechatReplyMessage;



public class WechatMessageUtil {

	protected static final Logger logger = Logger.getLogger(WechatMessageUtil.class);
	
	private static String USERURL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
	/**
	 * 创建活动订单推送
	 * @param order
	 * @param setting
	 * @param orderUrl
	 * @return
	 * {{first.DATA}}
		订单编号：{{keyword1.DATA}}
		订单类型：{{keyword2.DATA}}
		订单金额：{{keyword3.DATA}}
		下单时间：{{keyword4.DATA}}
		{{remark.DATA}}
	 */
	public static String newActivityCreateOrderNotification(final SasMenuActivityOrder order, final SasWechatNotificationPerson person, final String orderUrl) {
		String openId = person.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(1).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(1).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有活动下单啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(order.getOrderCode());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(order.getActivityTitle());
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(order.getUserNickname());
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData keyword4 = new TemplateData();
		keyword4.setValue(order.getTotalPriceWithRefundAmount()+"");
		keyword4.setColor("#173177");
		m.put("keyword4", keyword4);
		TemplateData keyword5 = new TemplateData();
		keyword5.setValue(TimeUtil.formatDate(order.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword5.setColor("#173177");
		m.put("keyword5", keyword5);
		TemplateData remark = new TemplateData();
		remark.setValue("您还可以在“详情”中查看订单");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(orderUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}
	
	
	/**
	 * 创建商品订单推送
	 * @param order
	 * @param setting
	 * @param orderUrl
	 * @return
	 * {{first.DATA}}
		订单编号：{{keyword1.DATA}}
		订单类型：{{keyword2.DATA}}
		订单金额：{{keyword3.DATA}}
		下单时间：{{keyword4.DATA}}
		{{remark.DATA}}
	 */
	public static String newGoodCreateOrderNotification(final SasMenuGoodOrder order, final SasWechatNotificationPerson person, final String orderUrl, final List<SasMenuGoodOrderItem> items) {
		String openId = person.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(1).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(1).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有商品下单啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(order.getOrderCode());//商品订单ID
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(items.get(0).getGoodTitle() + "等");
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(order.getUserNickname());
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData keyword4 = new TemplateData();
		keyword4.setValue(order.getTotalPriceWithRefundAmount()+"");
		keyword4.setColor("#173177");
		m.put("keyword4", keyword4);
		TemplateData keyword5 = new TemplateData();
		keyword5.setValue(TimeUtil.formatDate(order.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword5.setColor("#173177");
		m.put("keyword5", keyword5);
		TemplateData remark = new TemplateData();
		remark.setValue("您还可以在“详情”中查看订单");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(orderUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}
	
	/**
	 * 活动订单支付成功推送
	 * @param order
	 * @param setting
	 * @param orderUrl
	 * @return
	 * {{first.DATA}}
		订单类型：{{keyword1.DATA}}
		姓名：{{keyword2.DATA}}
		订单金额：{{keyword3.DATA}}
		订单时间：{{keyword4.DATA}}
		订单编号：{{keyword5.DATA}}
		{{remark.DATA}}
	 */
	public static String newActivityWechatPayNotification(final SasMenuActivityOrder order , final SasWechatNotificationPerson person, final String orderUrl) {
		String openId = person.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(2).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(2).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有用户支付啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(order.getOrderCode());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(order.getActivityTitle());
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(order.getUserNickname());
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData keyword4 = new TemplateData();
		keyword4.setValue(order.getTotalPriceWithRefundAmount()+"元");
		keyword4.setColor("#173177");
		m.put("keyword4", keyword4);
		TemplateData keyword5 = new TemplateData();
		keyword5.setValue(TimeUtil.formatDate((order.getPayTime() > Miliseconds.OneDay.miliseconds ? order.getPayTime() : order.getCreateTime()), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword5.setColor("#173177");
		m.put("keyword5", keyword5);
		TemplateData remark = new TemplateData();
		remark.setValue("您还可以在“详情”中查看订单");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(orderUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}
	
	
	/**
	 * 商品订单支付成功推送
	 * @param order
	 * @param setting
	 * @param orderUrl
	 * @return
	 * {{first.DATA}}
		订单类型：{{keyword1.DATA}}
		姓名：{{keyword2.DATA}}
		订单金额：{{keyword3.DATA}}
		订单时间：{{keyword4.DATA}}
		订单编号：{{keyword5.DATA}}
		{{remark.DATA}}
	 */
	public static String newGoodWechatPayNotification(final SasMenuGoodOrder order , final SasWechatNotificationPerson person, 
			final String orderUrl, final List<SasMenuGoodOrderItem> items ) {
		String openId = person.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(2).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(2).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有用户支付啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(order.getOrderCode());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(items.get(0).getGoodTitle() + "等");
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(order.getUserNickname());
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData keyword4 = new TemplateData();
		keyword4.setValue(order.getTotalPriceWithRefundAmount()+"元");
		keyword4.setColor("#173177");
		m.put("keyword4", keyword4);
		TemplateData keyword5 = new TemplateData();
		keyword5.setValue(TimeUtil.formatDate((order.getPayTime() > Miliseconds.OneDay.miliseconds ? order.getPayTime() : order.getCreateTime()), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword5.setColor("#173177");
		m.put("keyword5", keyword5);
		TemplateData remark = new TemplateData();
		remark.setValue("您还可以在“详情”中查看订单");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(orderUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}

	/**
	 * 取消活动订单推送
	 * @param order
	 * @param setting
	 * @param orderUrl
	 * @return
	 * {{first.DATA}}
		订单编号：{{keyword1.DATA}}
		活动名称：{{keyword2.DATA}}
		活动日期：{{keyword3.DATA}}
		订单金额：{{keyword4.DATA}}
		{{remark.DATA}}
	 */
	public static String newActivityCancelOrderNitification(final SasMenuActivityOrder order , final SasWechatNotificationPerson person, final String orderUrl) {
		String openId = person.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(3).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(3).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有用户退单啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(order.getOrderCode());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(order.getActivityTitle());
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(order.getUserNickname());
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData keyword4 = new TemplateData();
		keyword4.setValue(order.getTotalPriceWithRefundAmount()+"元");
		keyword4.setColor("#173177");
		m.put("keyword4", keyword4);
		TemplateData keyword5 = new TemplateData();
		keyword5.setValue(TimeUtil.formatDate(order.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword5.setColor("#173177");
		m.put("keyword5", keyword5);
		TemplateData remark = new TemplateData();
		remark.setValue("您还可以在“详情”中查看订单");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(orderUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}
	
	/**
	 * 商品订单取消推送
	 * @param order
	 * @param setting
	 * @param orderUrl
	 * @return
	 * {{first.DATA}}
		订单编号：{{keyword1.DATA}}
		商品详情：{{keyword2.DATA}}
		订单金额：{{keyword3.DATA}}
		{{remark.DATA}}
	 */
	public static String newGoodCancelOrderNotification(final SasMenuGoodOrder order,
			final List<SasMenuGoodOrderItem> items, final SasWechatNotificationPerson person, final String orderUrl) {
		String openId = person.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(8).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(8).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有用户退单啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(order.getOrderCode());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(items.get(0).getGoodTitle() + "等");
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(order.getUserNickname());
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData keyword4 = new TemplateData();
		keyword4.setValue(order.getTotalPriceWithRefundAmount()+"元");
		keyword4.setColor("#173177");
		m.put("keyword4", keyword4);
		TemplateData keyword5 = new TemplateData();
		keyword5.setValue(TimeUtil.formatDate(order.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword5.setColor("#173177");
		m.put("keyword5", keyword5);
		TemplateData remark = new TemplateData();
		remark.setValue("您还可以在“详情”中查看订单");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(orderUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}

	 /**
	  * 活动评论推送
	  * @param activity
	  * @param activityComment
	  * @param setting
	  * @param commentUrl
	  * @return
	  */
	public static String newActivityCommentNotification(final SasMenuActivity activity, final SasMenuActivityComment activityComment , final SasWechatNotificationSetting setting, final String commentUrl) {
		String openId = setting.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(4).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(4).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有用户评论活动啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(activity.getTitle());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(activityComment.getUserNickname());
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(TimeUtil.formatDate(activityComment.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData remark = new TemplateData();
		remark.setValue("您还在“详情”里查看评论内容");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(commentUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}

	
	/**
	 * 商品评论推送
	 * @param good
	 * @param comment
	 * @param setting
	 * @param commentUrl
	 * @return
	 */
	public static String newGoodConsultNotification(final SasMenuGood good, final SasMenuGoodComment comment , final SasWechatNotificationSetting setting, final String commentUrl) {
		String openId = setting.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(5).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(5).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有用户咨询商品啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(good.getTitle());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(comment.getUserNickname());
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(TimeUtil.formatDate(comment.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData remark = new TemplateData();
		remark.setValue("您还在“详情”里查看咨询内容");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(commentUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}

	/**
	 * 文章评论推送
	 * @param article
	 * @param articleComment
	 * @param setting
	 * @param commentUrl
	 * @return
	 */
	public static String newArticleCommentNotification(final SasMenuArticle article, final SasMenuArticleComment articleComment , final SasWechatNotificationSetting setting, final String commentUrl) {
		String openId = setting.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(6).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(6).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("文章有新评论啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(article.getTitle());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(articleComment.getUserNickname());
		keyword2.setColor("#173177");
		m.put("keyword2", keyword2);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(TimeUtil.formatDate(articleComment.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData remark = new TemplateData();
		remark.setValue("您还在“详情”里查看评论内容");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(commentUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}

	/**
	 * 论坛主题发布推送
	 * @param bbsForumReply
	 * @param setting
	 * @param topicUrl
	 * @return
	 */
	public static String newAddTopicNotification(final SasMenuBbsForum bbsForum , final SasWechatNotificationSetting setting, final String topicUrl) {
		String openId = setting.getRemindOpenId();
		String templatId = SasWechatNotificationConstant.SettingMessageType.parse(7).templatId;
//		String templatId = SasWechatSwitchConstant.SettingMessageTypeTest.parse(7).templatId;
		Map<String, TemplateData> m = new HashMap<String, TemplateData>();
		TemplateData first = new TemplateData();
		first.setValue("有新话题发表啦~");
		first.setColor("#173177");
		m.put("first", first);
		TemplateData keyword1 = new TemplateData();
		keyword1.setValue(bbsForum.getTitle());
		keyword1.setColor("#173177");
		m.put("keyword1", keyword1);
		TemplateData keyword2 = new TemplateData();
		keyword2.setValue(bbsForum.getUserNickname());
		keyword2.setColor("#173177");
		m.put("keyword2", keyword1);
		TemplateData keyword3 = new TemplateData();
		keyword3.setValue(TimeUtil.formatDate(bbsForum.getCreateTime(), TimeFormat.yyyy_MM_dd_HH_mm));
		keyword3.setColor("#173177");
		m.put("keyword3", keyword3);
		TemplateData remark = new TemplateData();
		remark.setValue("您还在“详情”里查看话题内容");
		remark.setColor("#173177");
		m.put("remark", remark);
		WxTemplate template = new WxTemplate();
		template.setData(m);
		template.setTemplate_id(templatId);
		template.setTopcolor("#173177");
		template.setTouser(openId);
		template.setUrl(topicUrl);
		JSONObject json = new JSONObject(template);
		return json.toString();
	}
	
	
	/**
	 * 关注之后欢迎词
	 * @param map
	 * @return
	 */
	public static String sendWelcomeMessage(Map<String, String> map){
		// 发送方帐号（一个OpenID）
				String fromUserName = map.get("FromUserName");
				// 开发者微信号
				String toUserName = map.get("ToUserName");
				// 默认回复一个"success"
				String responseMessage = "success";
				// 对消息进行处理
					WechatReplyMessage textMessage = new WechatReplyMessage();
					textMessage.setMsgType(WechatMessageReply.MESSAGE_TEXT);
					textMessage.setToUserName(fromUserName);
					textMessage.setFromUserName(toUserName);
					textMessage.setCreateTime(System.currentTimeMillis());
					String message = transformCoding(AutoContentType.parse("欢迎词").message);
					textMessage.setContent(message);
					responseMessage = WechatMessageReply.textMessageToXml(textMessage);
			return responseMessage;
	}
	
	
	/**
	 * 获取微信用户昵称
	 * @param openId
	 * @param accessToken
	 * @return
	 */
	public  static String getNickname(String openId , String accessToken){
		String result = "";
		try {
			String url = USERURL.replace("ACCESS_TOKEN", accessToken);
		 url = url.replace("OPENID", openId);
		 result = SasWechatNotificationUtil.httpsRequest(url, "GET",
						null);
		
			JSONObject user = new JSONObject(result);
			result = user.getString("nickname");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	 * 处理微信消息请求
	 * @param msgType
	 * @return
	 */
	public static String messageReply(String msgType , Map<String, String> map){
		String message = "";
		if (WechatMessageReply.MESSAGE_TEXT.equalsIgnoreCase(msgType)) {
			message = messageReplyText(map);
		}
		if (WechatMessageReply.MESSAtGE_IMAGE.equalsIgnoreCase(msgType)) {
			message = messageReplyImage(map);
		}
		if (WechatMessageReply.MESSAGE_NEWS.equalsIgnoreCase(msgType)) {
			message = messageReplyNews(map);
		}
		if (WechatMessageReply.MESSAGE_VOICE.equalsIgnoreCase(msgType)) {
			message = messageReplyVoice(map);
		}
		if (WechatMessageReply.MESSAGE_SHORTVIDEO.equalsIgnoreCase(msgType)) {
			message = messageReplyShortVideo(map);
		}
		if (WechatMessageReply.MESSAGE_LOCATION.equalsIgnoreCase(msgType)) {
			message = messageReplyLocation(map);
		}
		if (WechatMessageReply.MESSAGE_LINK.equalsIgnoreCase(msgType)) {
			message = messageReplyLink(map);
		}
		if(WechatMessageReply.MESSAGE_EVENT_CLICK.equalsIgnoreCase(msgType)){
			message = messageReplyClick(map);
		}
		return message;
	}
	
	/**
	 * 文本信息回复
	 * @param map
	 * @return
	 */
	public static String messageReplyText(Map<String, String> map){
		// 发送方帐号（一个OpenID）
		String fromUserName = map.get("FromUserName");
		// 开发者微信号
		String toUserName = map.get("ToUserName");
		// 消息类型
		String msgType = map.get("MsgType");
		// 默认回复一个"success"
		String responseMessage = "success";
		// 对消息进行处理
		if (WechatMessageReply.MESSAGE_TEXT.equals(msgType)) {// 文本消息
			 AutoContentType autoContentType = AutoContentType.parse(map.get("Content"));
			 WechatReplyMessage textMessage = new WechatReplyMessage();
			 textMessage.setMsgType(WechatMessageReply.MESSAGE_TEXT);
			 textMessage.setToUserName(fromUserName);
			 textMessage.setFromUserName(toUserName);
			 textMessage.setCreateTime(System.currentTimeMillis());
			 String message = transformCoding(autoContentType.message);
			 textMessage.setContent(message);
			 responseMessage = WechatMessageReply.textMessageToXml(textMessage);
			
		}
	return responseMessage;
	}
	
	/**
	 * 图片信息回复
	 * @param map
	 * @return
	 */
	public static String messageReplyImage(Map<String, String> map){
		String message = "";
		return message;
	}
	
	/**
	 * 图文信息回复
	 * @param map
	 * @return
	 */
	public static String messageReplyNews(Map<String, String> map){
		String message = "";
		return message;
	}
	
	/**
	 * 语音信息回复
	 * @param map
	 * @return
	 */
	public static String messageReplyVoice(Map<String, String> map){
		String message = "";
		return message;
	}
	
	/**
	 * 小视频信息回复
	 * @param map
	 * @return
	 */
	public static String messageReplyShortVideo(Map<String, String> map){
		String message = "";
		return message;
	}
	
	/**
	 * 地理信息回复
	 * @param map
	 * @return
	 */
	public static String messageReplyLocation(Map<String, String> map){
		String message = "";
		return message;
	}
	
	/**
	 * 链接信息回复
	 * @param map
	 * @return
	 */
	public static String messageReplyLink(Map<String, String> map){
		String message = "";
		return message;
	}
	
	public static String messageReplyClick(Map<String, String> map){
		// 发送方帐号（一个OpenID）
				String fromUserName = map.get("FromUserName");
				// 开发者微信号
				String toUserName = map.get("ToUserName");
				// 默认回复一个"success"
				String responseMessage = "success";
				// 对消息进行处理
					WechatReplyMessage textMessage = new WechatReplyMessage();
					textMessage.setMsgType(WechatMessageReply.MESSAGE_TEXT);
					textMessage.setToUserName(fromUserName);
					textMessage.setFromUserName(toUserName);
					textMessage.setCreateTime(System.currentTimeMillis());
					String message = transformCoding(WechatClickConstant.parse(map.get("EventKey")).message);
					textMessage.setContent(message);
					responseMessage = WechatMessageReply.textMessageToXml(textMessage);
			return responseMessage;
	}
	
	public static void main(String[] args) {
		long switchState = WechatSwitchState.defaultValue(); //开关状态
		System.out.println(switchState);
	}
	
	
	/**
	 * 微信主动回复内容编码信息处理
	 * @param message
	 * @return
	 */
	public static String transformCoding(String message){
		try {
			message = new String(message.getBytes("UTF-8"),"iso8859-1");
		} catch (UnsupportedEncodingException e) {
			logger.error("WechatMessageUtil : " + e.getMessage(), e);
			
			
		}
		return message;
		
	}
}


