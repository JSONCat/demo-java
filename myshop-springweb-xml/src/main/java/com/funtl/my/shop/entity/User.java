package com.funtl.my.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
@Data
@AllArgsConstructor
public class User implements Serializable {
  private String username;
  private String password;
  private String email;
}
