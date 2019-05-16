package com.funtl.hello.mvc.service.impl;

import com.funtl.hello.mvc.dao.UserDao;
import com.funtl.hello.mvc.dao.impl.UserDaoImpl;
import com.funtl.hello.mvc.entity.User;
import com.funtl.hello.mvc.service.UserService;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/16
 */
public class UserServiceImpl implements UserService {
  private UserDao userDao = new UserDaoImpl();
  
  @Override
  public User login(String loginId, String loginPwd) {
    
    return userDao.login(loginId,loginPwd);
  }
}
