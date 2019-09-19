package com.sas.core.util.wxpay.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import com.sas.core.util.wxpay.WXPayConstant;
import com.sas.core.util.wxpay.meta.ScanPayQueryReqData;

/**
 * User: rizenguo Date: 2014/10/29 Time: 16:04
 */
public class ScanPayQueryService extends BaseService {

	public ScanPayQueryService(final String certPassword, final String certLocalPath) throws IllegalAccessException, 
		InstantiationException, ClassNotFoundException, UnrecoverableKeyException, KeyManagementException,
		NoSuchAlgorithmException, KeyStoreException, IOException {
		super(certPassword, certLocalPath,WXPayConstant.PAY_QUERY_API);
	}

	/**
	 * 请求支付查询服务
	 * 
	 * @param scanPayQueryReqData
	 *            这个数据对象里面包含了API要求提交的各种数据字段
	 * @return API返回的XML数据
	 * @throws Exception
	 */
	public String request(ScanPayQueryReqData scanPayQueryReqData) throws Exception {

		// --------------------------------------------------------------------
		// 发送HTTPS的Post请求到API地址
		// --------------------------------------------------------------------
		String responseString = sendPost(scanPayQueryReqData);

		return responseString;
	}
}