package com.sas.core.util.wechat.api;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;

import com.sas.core.util.wechat.meta.BaseResult;
import com.sas.core.util.wechat.meta.Media;
import com.sas.core.util.wechat.meta.ApiAddTemplateResult;
import com.sas.core.util.wechat.meta.Article;
import com.sas.core.util.wechat.meta.CurrentAutoreplyInfo;
import com.sas.core.util.wechat.meta.GetAllPrivateTemplateResult;
import com.sas.core.util.wechat.meta.GetIndustryResult;
import com.sas.core.util.wechat.meta.MessageSendResult;
import com.sas.core.util.wechat.meta.Uploadvideo;
//import com.sas.core.util.wechat.meta.MassMessage;
import com.sas.core.util.wechat.meta.Message;
import com.sas.core.util.wechat.meta.Preview;
import com.sas.core.util.wechat.meta.TemplateMessage;
import com.sas.core.util.wechat.meta.TemplateMessageResult;
import com.sas.core.util.wechat.client.LocalHttpClient;
import com.sas.core.util.wechat.utils.JsonUtil;

/**
 * 当用户主动发消息给公众号的时候
 * （包括发送信息、点击自定义菜单click事件、订阅事件、扫描二维码事件、支付成功事件、用户维权），
 * 微信将会把消息数据推送给开发者，
 * 开发者在一段时间内（目前修改为48小时）可以调用客服消息接口，
 * 通过POST一个JSON数据包来发送消息给普通用户，
 * 在48小时内不限制发送次数。
 * 此接口主要用于客服等有人工消息处理环节的功能，方便开发者为用户提供更加优质的服务。
 * @author LiYi
 *
 */
public class MessageAPI extends BaseAPI{


	/**
	 * 消息发送
	 * @param access_token
	 * @param messageJson
	 * @return
	 */
	public static BaseResult messageCustomSend(String access_token,String messageJson){
		HttpUriRequest httpUriRequest = RequestBuilder.post()
										.setHeader(jsonHeader)
										.setUri(BASE_URI+"/cgi-bin/message/custom/send")
										.addParameter(getATPN(), access_token)
										.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
										.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,BaseResult.class);
	}

	/**
	 * 消息发送
	 * @param access_token
	 * @param message
	 * @return
	 */
	public static BaseResult messageCustomSend(String access_token,Message message){
		String str = JsonUtil.toJSONString(message);
		return messageCustomSend(access_token,str);
	}

	/**
	 * 高级群发 构成 MassMPnewsMessage 对象的前置请求接口
	 * @param access_token
	 * @param articles 图文信息 1-10 个
	 * @return
	 */
	public static Media mediaUploadnews(String access_token,List<Article> articles){
		String str = JsonUtil.toJSONString(articles);
		String messageJson = "{\"articles\":"+str+"}";
		HttpUriRequest httpUriRequest = RequestBuilder.post()
										.setHeader(jsonHeader)
										.setUri(BASE_URI+"/cgi-bin/media/uploadnews")
										.addParameter(getATPN(), access_token)
										.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
										.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,Media.class);
	}

	/**
	 * 高级群发 构成 MassMPvideoMessage 对象的前置请求接口
	 * @param access_token
	 * @param uploadvideo
	 * @return
	 */
	public static Media mediaUploadvideo(String access_token,Uploadvideo uploadvideo){
		String messageJson = JsonUtil.toJSONString(uploadvideo);
		HttpUriRequest httpUriRequest = RequestBuilder.post()
										.setHeader(jsonHeader)
										.setUri(MEDIA_URI+"/cgi-bin/media/uploadvideo")
										.addParameter(getATPN(), access_token)
										.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
										.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,Media.class);
	}


	/**
	 * 高级群发接口 根据分组进行群发
	 * @param access_token
	 * @param messageJson
	 * @return
	 */
	public static MessageSendResult messageMassSendall(String access_token,String messageJson){
		HttpUriRequest httpUriRequest = RequestBuilder.post()
										.setHeader(jsonHeader)
										.setUri(BASE_URI+"/cgi-bin/message/mass/sendall")
										.addParameter(getATPN(), access_token)
										.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
										.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,MessageSendResult.class);
	}

	/**
	 * 高级群发接口 根据OpenID列表群发
	 * @param access_token
	 * @param messageJson
	 * @return
	 */
	public static MessageSendResult messageMassSend(String access_token,String messageJson){
		HttpUriRequest httpUriRequest = RequestBuilder.post()
										.setHeader(jsonHeader)
										.setUri(BASE_URI+"/cgi-bin/message/mass/send")
										.addParameter(getATPN(), access_token)
										.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
										.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,MessageSendResult.class);
	}

	/**
	 * 高级群发接口 根据OpenID列表群发
	 * @param access_token
	 * @param massMessage
	 * @return
	 */
//	public static MessageSendResult messageMassSend(String access_token,MassMessage massMessage){
//		String str = JsonUtil.toJSONString(massMessage);
//		return messageMassSend(access_token,str);
//	}


	/**
	 * 高级群发接口	删除群发
	 * 请注意，只有已经发送成功的消息才能删除删除消息只是将消息的图文详情页失效，
	 * 已经收到的用户，还是能在其本地看到消息卡片。
	 * 另外，删除群发消息只能删除图文消息和视频消息，其他类型的消息一经发送，无法删除。
	 * @param access_token
	 * @param msg_id
	 * @return
	 */
	public static BaseResult messageMassDelete(String access_token,String msg_id){
		String messageJson = String.format("{\"msg_id\":\"%s\"}",msg_id);
		HttpUriRequest httpUriRequest = RequestBuilder.post()
										.setHeader(jsonHeader)
										.setUri(BASE_URI+"/cgi-bin/message/mass/delete")
										.addParameter(getATPN(), access_token)
										.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
										.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,BaseResult.class);
	}

	/**
	 * 预览接口
	 * @since 2.6.3
	 * @param access_token
	 * @param preview
	 * @return
	 */
	public static MessageSendResult messageMassPreview(String access_token,Preview preview){
		String previewJson = JsonUtil.toJSONString(preview);
		HttpUriRequest httpUriRequest = RequestBuilder.post()
										.setHeader(jsonHeader)
										.setUri(BASE_URI+"/cgi-bin/message/mass/preview")
										.addParameter(getATPN(), access_token)
										.setEntity(new StringEntity(previewJson,Charset.forName("utf-8")))
										.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,MessageSendResult.class);
	}
	
	/**
	 * 查询群发消息发送状态
	 * @since 2.6.3
	 * @param access_token
	 * @param msg_id
	 * @return
	 */
	public static MessageSendResult messageMassGet(String access_token,String msg_id){
		String messageJson = String.format("{\"msg_id\":\"%s\"}",msg_id);
		HttpUriRequest httpUriRequest = RequestBuilder.post()
										.setHeader(jsonHeader)
										.setUri(BASE_URI+"/cgi-bin/message/mass/get")
										.addParameter(getATPN(), access_token)
										.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
										.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,MessageSendResult.class);
	}


	/**
	 * 模板消息发送
	 * @param access_token
	 * @param templateMessage
	 * @return
	 */
	public static TemplateMessageResult messageTemplateSend(String access_token,TemplateMessage templateMessage){
		String messageJson = JsonUtil.toJSONString(templateMessage);
		HttpUriRequest httpUriRequest = RequestBuilder.post()
				.setHeader(jsonHeader)
				.setUri(BASE_URI+"/cgi-bin/message/template/send")
				.addParameter(getATPN(), access_token)
				.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
				.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,TemplateMessageResult.class);
	}

	/**
	 * 模板消息 设置所属行业
	 * @since 2.6.1
	 * @param access_token
	 * @param industrys 行业值，暂设置个数限制为2个。
	 *  
		主行业	副行业	代码
		IT科技	互联网/电子商务	1
		IT科技	IT软件与服务	2
		IT科技	IT硬件与设备	3
		IT科技	电子技术	4
		IT科技	通信与运营商	5
		IT科技	网络游戏	6
		金融业	银行	7
		金融业	基金|理财|信托	8
		金融业	保险	9
		餐饮	餐饮	10
		酒店旅游	酒店	11
		酒店旅游	旅游	12
		运输与仓储	快递	13
		运输与仓储	物流	14
		运输与仓储	仓储	15
		教育	培训	16
		教育	院校	17
		政府与公共事业	学术科研	18
		政府与公共事业	交警	19
		政府与公共事业	博物馆	20
		政府与公共事业	公共事业|非盈利机构	21
		医药护理	医药医疗	22
		医药护理	护理美容	23
		医药护理	保健与卫生	24
		交通工具	汽车相关	25
		交通工具	摩托车相关	26
		交通工具	火车相关	27
		交通工具	飞机相关	28
		房地产	建筑	29
		房地产	物业	30
		消费品	消费品	31
		商业服务	法律	32
		商业服务	会展	33
		商业服务	中介服务	34
		商业服务	认证	35
		商业服务	审计	36
		文体娱乐	传媒	37
		文体娱乐	体育	38
		文体娱乐	娱乐休闲	39
		印刷	印刷	40
		其它	其它	41
	 * @return
	 */
	public static BaseResult templateApi_set_industry(String access_token,String... industrys){
		Map<String, String> map = new LinkedHashMap<String, String>();
		for(int i=1;i<=industrys.length;i++){
			map.put("industry_id"+i, industrys[i-1]);
		}
		String messageJson = JsonUtil.toJSONString(map);
		HttpUriRequest httpUriRequest = RequestBuilder.post()
				.setHeader(jsonHeader)
				.setUri(BASE_URI+"/cgi-bin/template/api_set_industry")
				.addParameter(getATPN(), access_token)
				.setEntity(new StringEntity(messageJson,Charset.forName("utf-8")))
				.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,BaseResult.class);
	}
	
	/**
	 * 获取设置的行业信息
	 * @since 2.6.1
	 * @param access_token
	 * @return
	 */
	public static GetIndustryResult templateGet_industry(String access_token){
		HttpUriRequest httpUriRequest = RequestBuilder.post()
				.setUri(BASE_URI+"/cgi-bin/template/get_industry")
				.addParameter(getATPN(), access_token)
				.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,GetIndustryResult.class);
	}
	
	/**
	 * 模板消息 获得模板ID
	 * @since 2.6.1
	 * @param access_token
	 * @param template_id_short
	 * @return
	 */
	public static ApiAddTemplateResult templateApi_add_template(String access_token,String template_id_short){
		String json = String.format("{\"template_id_short\":\"%s\"}",template_id_short);
		HttpUriRequest httpUriRequest = RequestBuilder.post()
				.setHeader(jsonHeader)
				.setUri(BASE_URI+"/cgi-bin/template/api_add_template")
				.addParameter(getATPN(), access_token)
				.setEntity(new StringEntity(json,Charset.forName("utf-8")))
				.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,ApiAddTemplateResult.class);
	}
	
	/**
	 * 模板消息 获取模板列表
	 * @since 2.6.1
	 * @param access_token
	 * @return
	 */
	public static GetAllPrivateTemplateResult templateGet_all_private_template(String access_token){
		HttpUriRequest httpUriRequest = RequestBuilder.post()
				.setUri(BASE_URI+"/cgi-bin/template/get_all_private_template")
				.addParameter(getATPN(), access_token)
				.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,GetAllPrivateTemplateResult.class);
	}
	
	/**
	 * 模板消息 删除模板
	 * @since 2.6.1
	 * @param access_token
	 * @param template_id
	 * @return
	 */
	public static BaseResult templateDel_private_template(String access_token,String template_id){
		String json = String.format("{\"template_id\":\"%s\"}",template_id);
		HttpUriRequest httpUriRequest = RequestBuilder.post()
				.setHeader(jsonHeader)
				.setUri(BASE_URI+"/cgi-bin/template/del_private_template")
				.addParameter(getATPN(), access_token)
				.setEntity(new StringEntity(json,Charset.forName("utf-8")))
				.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,BaseResult.class);
	}
	
	/**
	 * 获取公众号的自动回复规则
	 * @since 2.6.3
	 * @param access_token
	 * @return
	 */
	public static CurrentAutoreplyInfo get_current_autoreply_info(String access_token){
		HttpUriRequest httpUriRequest = RequestBuilder.post()
				.setUri(BASE_URI+"/cgi-bin/get_current_autoreply_info")
				.addParameter(getATPN(), access_token)
				.build();
		return LocalHttpClient.executeJsonResult(httpUriRequest,CurrentAutoreplyInfo.class);
	}
}
