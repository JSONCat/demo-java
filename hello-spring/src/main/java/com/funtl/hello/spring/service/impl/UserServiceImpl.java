package com.funtl.hello.spring.service.impl;

import com.funtl.hello.spring.service.UserSetvice;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/17
 */
public class UserServiceImpl implements UserSetvice {
  @Override
  public void sayHi() {
    System.out.println("Hello Spring!");
  }
}
