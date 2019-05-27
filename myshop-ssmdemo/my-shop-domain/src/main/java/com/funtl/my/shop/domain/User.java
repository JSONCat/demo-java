package com.funtl.my.shop.domain;


import java.io.Serializable;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
public class User implements Serializable {
  private String username;
  private String password;
  private String email;
  private boolean isRemenber;
  
  public User(String username, String password, String email) {
    this.username = username;
    this.password = password;
    this.email = email;
  }
  
  public String getUsername() {
    return username;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public boolean isRemenber() {
    return isRemenber;
  }
  
  public void setRemenber(boolean remenber) {
    isRemenber = remenber;
  }
}
