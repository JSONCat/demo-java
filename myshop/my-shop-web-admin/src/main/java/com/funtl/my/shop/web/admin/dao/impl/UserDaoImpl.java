package com.funtl.my.shop.web.admin.dao.impl;

import com.funtl.my.shop.web.admin.dao.UserDao;
import com.funtl.my.shop.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
@Repository
public class UserDaoImpl implements UserDao {
  
  @Override
  public User getByEmail(String email) {
    return new User("admin","123456",email);
  }
}
