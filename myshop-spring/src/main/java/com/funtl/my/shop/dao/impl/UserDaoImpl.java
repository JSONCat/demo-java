package com.funtl.my.shop.dao.impl;

import com.funtl.my.shop.dao.UserDao;
import com.funtl.my.shop.entity.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
public class UserDaoImpl implements UserDao {
  
  @Override
  public User getByEmail(String email) {
    return new User("admin","123456",email);
  }
}
