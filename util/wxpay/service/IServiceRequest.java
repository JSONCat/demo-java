package com.sas.core.util.wxpay.service;


/**
 * User: rizenguo
 * Date: 2014/12/10
 * Time: 15:16
 * 这里定义服务层需要请求器标准接口
 */
public interface IServiceRequest {
    //Service依赖的底层https请求器必须实现这么一个接口
    public String sendPost(String api_url,Object xmlObj) throws Exception;
}