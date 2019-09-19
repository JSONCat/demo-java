package com.funtl.my.shop.domain;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/27
 */
public class BaseMeta implements Serializable {
  
  /************
   * 转化成string
   */
  public String toString(){
    return ReflectionToStringBuilder.toString(this);
  }
}
