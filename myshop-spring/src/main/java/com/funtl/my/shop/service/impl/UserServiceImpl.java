package com.funtl.my.shop.service.impl;

import com.funtl.my.shop.commons.context.SpringContext;
import com.funtl.my.shop.dao.UserDao;
import com.funtl.my.shop.entity.User;
import com.funtl.my.shop.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
public class UserServiceImpl implements UserService {
  private static  final Logger logger = LoggerFactory.getLogger(UserService.class);
  
  
  
  /**
   *
   * @param email
   * @param password
   * @return
   */
  @Override
  public User login(String email, String password) {
    UserDao userDao = (UserDao) SpringContext.context.getBean("userDao");
    User user = userDao.getByEmail(email);
    if (user.getPassword().equals(password)) {
      logger.info("成功获取{}的用户信息",user.getUsername());
      return user;
    }
    logger.info("{}对应的用户不存在",email);
    return null;
  }
}
