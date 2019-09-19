package com.sas.core.util.wxpay.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import com.sas.core.util.wxpay.WXPayConstant;
import com.sas.core.util.wxpay.meta.CloseReqData;

/**
 * User: rizenguo
 * Date: 2014/10/29
 * Time: 16:04
 */
public class CloseService extends BaseService{

    public CloseService(final String certPassword, final String certLocalPath) throws IllegalAccessException,
    	InstantiationException, ClassNotFoundException, UnrecoverableKeyException, KeyManagementException,
    	NoSuchAlgorithmException, KeyStoreException, IOException {
        super(certPassword, certLocalPath,WXPayConstant.CLOSE_API);
    }

    /**
     * 请求关闭订单服务
     * @param reverseReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
    public String request(CloseReqData closeReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(closeReqData);

        return responseString;
    }

}
