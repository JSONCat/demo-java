package com.funtl.hello.mvc.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/16
 */
@Data
public class User implements Serializable {
  private String username;
  private String loginId;
  private String loginPwd;
}
