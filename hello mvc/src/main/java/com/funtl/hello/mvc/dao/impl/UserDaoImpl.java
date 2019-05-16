package com.funtl.hello.mvc.dao.impl;

import com.funtl.hello.mvc.dao.UserDao;
import com.funtl.hello.mvc.entity.User;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/16
 */
public class UserDaoImpl implements UserDao {
  @Override
  public User login(String loginId, String loginPwd) {
    if ( "admin".equals(loginId)) {
      if ("admin".equals(loginPwd)){
        User user = new User();
        user.setLoginId("admin");
        user.setLoginPwd("admin");
        user.setUsername("admin");
        return user;
      }
    }
    return null;
  }
}
