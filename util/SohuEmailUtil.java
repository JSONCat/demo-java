/**
 * 
 */
package com.sas.core.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.sas.core.constant.CommonConstant.DividerChar;
import com.sas.core.constant.CommonConstant.Encoding;
import com.sas.core.constant.EmailConstant.SohuEmailAccount;
import com.sas.core.dto.EmailInfo;
import com.sas.core.exception.ServerUnknownException;
import com.sas.core.meta.EmailSendCloudDetail;
import com.sas.core.meta.Sas;
import com.sun.mail.smtp.SMTPTransport;

/**
 * 搜狐邮件util
 * @author Administrator
 *
 */
@SuppressWarnings("deprecation")
public class SohuEmailUtil {

	private static final Logger logger = Logger.getLogger("com.logger.email");
	
	public static final String apiKey = "B78rJay3xJ9YsAMM";
	
	public static final Properties emailProperties = System.getProperties();
	
	private static final String urlOfWebAPIUsingTemplate = "http://sendcloud.sohu.com/webapi/mail.send_template.json";
	
	private static final String urlOfWebAPIWithoutTemplate = "http://sendcloud.sohu.com/webapi/mail.send.json";
	   
	/*********************
	 * 新版的邮件发送方法
	 * @param sas
	 * @param emailDetail
	 * @throws Exception
	 */
	public static final void sendEmail2SohuServer(final Sas sas, final EmailSendCloudDetail emailDetail) throws Exception 
	{
		if(emailDetail == null || !emailDetail.hasReceiverEmails()){
			throw new ServerUnknownException("fail sending without any receiver emails!");
		}
		//设置基本信息
		SohuEmailAccount emailAccount = null;
		String fromName = null;
		if(sas == null){
			emailAccount = SohuEmailAccount.parse(emailDetail.getType(), null);			 
			fromName = "系统邮件通知";
		}else{
			emailAccount = SohuEmailAccount.parse(emailDetail.getType(), sas.getSubDomain());  
			fromName = sas.getName() + "网邮件通知";
		}
		SohuEmailUtil.sendEmail2SohuServer(emailAccount, fromName, emailDetail);
    }
	
	/*********************
	 * 发送邮件
	 * @param emailAccount
	 * @param fromName
	 * @param emailDetail
	 * @throws Exception
	 */
	private static final void sendEmail2SohuServer(final SohuEmailAccount emailAccount, final String fromName, final EmailSendCloudDetail emailDetail) throws Exception 
	{
		if(emailDetail == null || !emailDetail.hasReceiverEmails()){
			throw new ServerUnknownException("fail sending without any receiver emails!");
		}
        final String apiUser = emailAccount.apiUser;    
		final Session mailSession = Session.getInstance(SohuEmailUtil.emailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(apiUser, apiKey);
            }
        });
		final MimeMessage message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(emailAccount.fromEmail, fromName, Encoding.UTF8.type));  // 发信人
		// 收件人地址
        message.addRecipients(RecipientType.TO, emailDetail.getReceiverEmails());
        // 邮件主题
        message.setSubject(emailDetail.getSubject(), Encoding.UTF8.type);
        final Multipart multipart = new MimeMultipart("alternative");
        // 添加html形式的邮件正文
        final BodyPart contentPart = new MimeBodyPart();
        contentPart.setHeader("Content-Type", "text/html;charset=" + Encoding.UTF8.type);
        contentPart.setHeader("Content-Transfer-Encoding", "base64");
        contentPart.setContent(emailDetail.getBody(), "text/html;charset=UTF-8");
        multipart.addBodyPart(contentPart);
        SMTPTransport transport = (SMTPTransport) mailSession.getTransport("smtp");
        // 添加附件 ( smtp 方式没法使用文件流 )
		//File file = new File("/path/file");
		//BodyPart attachmentBodyPart = new MimeBodyPart();
		//DataSource source = new FileDataSource(file);
		//attachmentBodyPart.setDataHandler(new DataHandler(source));
		//attachmentBodyPart.setFileName(MimeUtility.encodeWord(file.getName()));
		//multipart.addBodyPart(attachmentBodyPart);
        message.setContent(multipart);

        //连接sendcloud服务器，发送邮件
        transport.connect();
        transport.sendMessage(message, message.getRecipients(RecipientType.TO));
        //String messageId = getMessage(transport.getLastServerResponse());
        logger.error("Email send response: " + transport.getLastServerResponse() + ", to=" + emailDetail.getReceiverEmails()
        		+ ", from=" + emailAccount.fromEmail);
        transport.close();
    }
	
	/**********************
	 * 
	 * @param emailAccount
	 * @param fromName
	 * @param emailDetail
	 * @param templateName
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static final void sendEmail2SohuServerUsingTemplate(final SohuEmailAccount emailAccount, 
			final String fromName, final EmailSendCloudDetail emailDetail, final String templateName) throws Exception
	{
		  final List<EmailInfo> dataList = new LinkedList<EmailInfo>();
		  //dataList.add(new A("to1@domain.com", "user1", "1000"));
		  for(final String receiverEmail : emailDetail.getReceiverEmails().split(DividerChar.Comma.chars)) {
			  dataList.add(new EmailInfo(receiverEmail, receiverEmail, ""));
		  }
		  final String vars = EmailInfo.convert(dataList);

		  final HttpClient httpclient = new DefaultHttpClient();
		  
		  final HttpPost httpost = new HttpPost(urlOfWebAPIUsingTemplate);

		  // 涉及到附件上传, 需要使用 MultipartEntity
		  final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName(Encoding.UTF8.type));
		  
		  entity.addPart("api_user", new StringBody(emailAccount.apiUser, Charset.forName(Encoding.UTF8.type)));
		  
		  entity.addPart("api_key", new StringBody(apiKey, Charset.forName(Encoding.UTF8.type)));
		  
		  entity.addPart("substitution_vars", new StringBody(vars, Charset.forName(Encoding.UTF8.type)));
		  
		  entity.addPart("template_invoke_name", new StringBody(templateName, Charset.forName(Encoding.UTF8.type)));
		  
		  entity.addPart("from", new StringBody(emailAccount.fromEmail, Charset.forName(Encoding.UTF8.type)));
		  
		  entity.addPart("fromname", new StringBody(fromName, Charset.forName(Encoding.UTF8.type)));
		  
		  entity.addPart("subject", new StringBody(emailDetail.getSubject(), Charset.forName(Encoding.UTF8.type)));
		  
		  entity.addPart("html", new StringBody(emailDetail.getBody(), Charset.forName(Encoding.UTF8.type)));
		  
		  entity.addPart("resp_email_id", new StringBody("true"));

		  // 添加附件
			//File file = new File("/home/liubida/Desktop/1");
			//FileBody attachment = new FileBody(file, "application/octet-stream", Encoding.UTF8.type);
			//entity.addPart("files", attachment);

		  // 添加附件, 文件流形式
		  // File file = new File("/path/file");
		  // String attachName = "attach.txt";
		  // InputStreamBody is = new InputStreamBody(new FileInputStream(file),
		  // attachName);
		  // entity.addPart("files", is);

		  httpost.setEntity(entity);
		  final HttpResponse response = httpclient.execute(httpost);
		  // 处理响应
		  if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {// 正常返回, 解析返回数据		      
		  	logger.error("Email send response: " + EntityUtils.toString(response.getEntity()));
		  } else {
		  	logger.error("Email send fail, subject=" + emailDetail.getSubject() + ", body=" + emailDetail.getBody());
		  }
		  httpost.releaseConnection();
	}
	
	/*****************
	 * 采用webapi， 不发送模板的名字
	 * @param emailAccount
	 * @param fromName
	 * @param emailDetail
	 * @throws Exception
	 */
	public static final void sendEmail2SohuServerUsingWebAPIWithoutTemplate(final SohuEmailAccount emailAccount, final String fromName, 
			final EmailSendCloudDetail emailDetail) throws Exception
	{	 
		final String[] emails = emailDetail.getReceiverEmails().split(DividerChar.Comma.chars);
		if(ArrayUtils.isEmpty(emails)){
			return;
		}
		final String firstEmail = emails[0];
		
	    HttpPost httpost = new HttpPost(urlOfWebAPIWithoutTemplate);
	    HttpClient httpclient = new DefaultHttpClient();

	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("api_user", emailAccount.apiUser));
	    params.add(new BasicNameValuePair("api_key", apiKey));
	    params.add(new BasicNameValuePair("to", firstEmail));
	    params.add(new BasicNameValuePair("from", emailAccount.fromEmail));
	    params.add(new BasicNameValuePair("fromname", fromName));
	    params.add(new BasicNameValuePair("subject", emailDetail.getSubject()));
	    params.add(new BasicNameValuePair("html", emailDetail.getBody()));
	    params.add(new BasicNameValuePair("resp_email_id", "true"));

	    httpost.setEntity(new UrlEncodedFormEntity(params, Encoding.UTF8.type));

	    HttpResponse response = httpclient.execute(httpost);

	    // response
	    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	    	logger.error("Email send response: " + EntityUtils.toString(response.getEntity()));
	    } else {
	    	logger.error("Email send fail, subject=" + emailDetail.getSubject() + ", body=" + emailDetail.getBody());
	    }
	    httpost.releaseConnection();
	}
}
