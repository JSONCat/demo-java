/**
 * 
 */
package com.sas.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sas.core.constant.CommonConstant;
import com.sas.core.constant.MenuConstant;
import com.sas.core.constant.SasWeixinConstant;
import com.sas.core.util.qiniu.StringUtils;
import com.sas.core.util.wechat.aes.AesException;
import com.sas.core.util.wechat.aes.WXBizMsgCrypt;
import com.sas.core.util.wechat.meta.EventMessage;
import com.sas.core.util.wechat.meta.XMLMessage;
import com.sas.core.util.wechat.meta.XMLNewsMessage;
import com.sas.core.util.wechat.meta.XMLTextMessage;
import com.sas.core.util.wechat.utils.StreamUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * 微信绑定的相关util方法
 * @author Administrator
 *
 */
public class WxBindUtil {

    public static final Logger logger = Logger.getLogger(WxBindUtil.class);

    /**
     * @Description: 解密微信通知消息
     *
     */
    public static String decryptMsg(String msgSignature, String timeStamp, String nonce, String postData) {
        try {
            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(
                    SasWeixinConstant.WeChatBind.EncodingToken.value,
                    SasWeixinConstant.WeChatBind.EncodingAESKey.value,
                    SasWeixinConstant.WeChatBind.ComponentAppId.value);

            return wxBizMsgCrypt.decryptMsg(
                    msgSignature, timeStamp, nonce, postData);

        } catch (AesException e) {
            logger.error("Fail to decryptMsg, err=" + e.getMessage() + ", " +
                    ",msg_mignature=" + msgSignature +
                    ",timestamp=" + timeStamp +
                    ",nonce=" + nonce +
                    ",postData=" + postData, e);
        }

        return null;
    }

    /**
     * @Description: 解密微信echo串
     *
     */
    public static String decryptEchostr(String msgSignature, String timestamp, String nonce, String echostr) {
        try {
            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(
                    SasWeixinConstant.WeChatBind.EncodingToken.value,
                    SasWeixinConstant.WeChatBind.EncodingAESKey.value,
                    SasWeixinConstant.WeChatBind.ComponentAppId.value);

            return wxBizMsgCrypt.verifyUrl(
                    msgSignature, timestamp, nonce, echostr);

        } catch (AesException e) {
            logger.error("Fail to decryptEchostr, err=" + e.getMessage() + ", " +
                    ",msg_mignature=" + msgSignature +
                    ",timestamp=" + timestamp +
                    ",nonce=" + nonce +
                    ",echostr=" + echostr, e);
        }

        return null;
    }

    /**
     * @Description: 加密微信回包
     *
     */
    public static String encryptMsg(String msg, String timestamp, String nonce) {
        try {
            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(
                    SasWeixinConstant.WeChatBind.EncodingToken.value,
                    SasWeixinConstant.WeChatBind.EncodingAESKey.value,
                    SasWeixinConstant.WeChatBind.ComponentAppId.value);

            return wxBizMsgCrypt.encryptMsg(msg, timestamp, nonce);

        } catch (AesException e) {
            logger.error("Fail to decryptEchostr, err=" + e.getMessage() + ", " +
                    ",msg=" + msg +
                    ",timestamp=" + timestamp +
                    ",nonce=" + nonce, e);
        }

        return null;
    }

    /**
     * @Description: 解密微信echo串
     *
     */
    public static RedirectView makeScanRedirectView(String sasDomain, String errorCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(sasDomain);
        sb.append("/admin/wechat/bind?mid=");
        sb.append(MenuConstant.ErpManageMenu.WexinOfficalAccount.id);
        sb.append(errorCode == null ? "#success" : ("#" + errorCode));
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(sb.toString());
        return redirectView;
    }

    public static String createResponse(String fromUser, String toUser, String json) {
        JSONObject obj = JSON.parseObject(json);
        if (obj != null) {
            XMLMessage toMessage = null;
            int type = obj.getIntValue("type");
            String msg = obj.getString("value");
            if (!StringUtils.isNullOrEmpty(msg)) {
                if (type == 0) {
                    toMessage = new XMLTextMessage(fromUser,toUser,msg);
                } else if (type == 1) {
                    toMessage = new XMLNewsMessage(fromUser,toUser,convertJsonToNewsList(msg));
                }
            }
            if (toMessage != null) {
                return toMessage.toXML();
            }
        }
        return null;
    }

    /**
     * @Description: 回复微信文本消息
     *
     */
    public static String responseTextMessage(EventMessage fromMessage, String text) {
        XMLMessage toMessage = new XMLTextMessage(fromMessage.getFromUserName(), fromMessage.getToUserName(), text);
        return toMessage.toXML();
    }


    /**
     * @Description: 生成文本群发消息JSON
     *
     */
    public static String createMassTextMessageJSON(long groupId, String content) {
        StringBuilder json = new StringBuilder();
        json.append("{\"filter\":{\"is_to_all\":");
        json.append(String.valueOf(groupId == -1));
        json.append(",\"group_id\":");
        json.append(groupId);
        json.append("},\"text\":{\"content\":\"");
        json.append(content);
        json.append("\"},");
        json.append("\"msgtype\":\"text\"");
        return json.toString();
    }

    /**
     * @Description: 生成图文群发消息JSON
     *
     */
    public static String createMassNewsMessageJSON(long groupId, String mediaId) {
        StringBuilder json = new StringBuilder();
        json.append("{\"filter\":{\"is_to_all\":");
        json.append(String.valueOf(groupId == 0));
        json.append(",\"group_id\":");
        json.append(groupId);
        json.append("},\"mpnews\":{\"media_id\":\"");
        json.append(mediaId);
        json.append("\"},");
        json.append("\"msgtype\":\"mpnews\"");
        return json.toString();
    }


    private static List<XMLNewsMessage.Article> convertJsonToNewsList(String json) {
        if (json.startsWith("{")) {
            JSONObject newsJson = JSON.parseObject(json);
            JSONObject articleJson = newsJson.getJSONObject("value");
            if (articleJson == null){
                return new LinkedList<XMLNewsMessage.Article>();
            }
            XMLNewsMessage.Article article = new XMLNewsMessage.Article();
            article.setTitle(articleJson.getString("title"));
            article.setDescription(articleJson.getString("digest"));
            article.setPicurl(articleJson.getString("thumb_url"));
            article.setUrl(articleJson.getString("url"));
            List<XMLNewsMessage.Article> articleList = new LinkedList<XMLNewsMessage.Article>();
            articleList.add(article);
            return articleList;
        } else {
            return JSON.parseArray(json, XMLNewsMessage.Article.class);
        }
    }

    public static void main(String[] args) {
        List<XMLNewsMessage.Article> list = convertJsonToNewsList("[{\"type\":0,\"title\":\"九峰桃花源摄影+丹霞巴寨奇观2天\",\"picurl\":\"http://img.saihuitong.com/5481/img/3693525/168f0ded844.jpg\",\"url\":\"http://168168000.com/event/?id=168277\",\"description\":\"\"},{\"type\":0,\"title\":\"连平赏鹰嘴桃花 三生三世十里桃花 矮山奇石 客家传奇燕翼围 南武当山+玻璃栈道+3D玻璃眺台两天游\",\"picurl\":\"http://img.saihuitong.com/5481/img/3693525/168e68e8c2c.jpg\",\"url\":\"http://168168000.com/event/?id=167670\",\"description\":\"\"},{\"type\":0,\"title\":\"【桃之夭夭，春意浓】——十里桃花三生三世赏连平醉美十里桃花\",\"picurl\":\"http://img.saihuitong.com/5481/img/3693525/168d51ab57d.jpg\",\"url\":\"http://168168000.com/event/?id=166925\",\"description\":\"\"}]");
        XMLMessage toMessage = new XMLNewsMessage(
                "receiver",
                "sender", list);
        String xml = toMessage.toXML();
        int x = 1;
    }
}
