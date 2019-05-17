package com.funtl.hello.spring.service.impl;

import com.funtl.hello.spring.service.TestSetvice;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/17
 */
public class TestServiceImpl implements TestSetvice {
  @Override
  public void sayHi() {
    System.out.println("Hello Spring!");
  }
}
