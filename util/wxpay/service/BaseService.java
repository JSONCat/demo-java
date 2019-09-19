package com.sas.core.util.wxpay.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.log4j.Logger;

import com.sas.core.constant.TimeConstant.Miliseconds;
import com.sas.core.util.ThreadUtil;
import com.sas.core.util.wxpay.HttpsRequest;

/**
 * User: rizenguo
 * Date: 2014/12/10
 * Time: 15:44
 * 服务的基类
 */
public class BaseService{

	public static final Logger logger = Logger.getLogger(BaseService.class);
	
    //API的地址
    private String apiURL;

    //发请求的HTTPS请求器
    private IServiceRequest serviceRequest;

    public BaseService(final String certPassword,
    		final String certLocalPath,
    		final String api) throws ClassNotFoundException, IllegalAccessException, InstantiationException,
    		UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
    	this.apiURL = api;
        this.serviceRequest = new HttpsRequest(certPassword, certLocalPath);
    }

    protected String sendPost(Object xmlObj) throws UnrecoverableKeyException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        int tryTimes = 0;
        do{
        	try{
        		return serviceRequest.sendPost(apiURL,xmlObj);
        	}catch(Exception ex){
        		logger.error("Fail to sendPost, ex=" + ex.getMessage(), ex);
        		tryTimes ++;
        		ThreadUtil.sleepNoException(Miliseconds.OneSecond.miliseconds * tryTimes);
        	}
        }while(tryTimes < 5);
        logger.error("Fail to sendPost, max try times=" + tryTimes);
        throw new IOException("Fail to sendPost, max try times=" + tryTimes);
    }

    /**
     * 供商户想自定义自己的HTTP请求器用
     * @param request 实现了IserviceRequest接口的HttpsRequest
     */
    public void setServiceRequest(IServiceRequest request){
        serviceRequest = request;
    }
}