package com.funtl.my.shop.service.impl;

import com.funtl.my.shop.service.HessianService;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/31
 */
public class HessianServerServiceImpl implements HessianService {
  @Override
  public void notice(String message) {
    System.out.println(String.format("接收到：",message,"的通知"));
  }
}
