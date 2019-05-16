package com.funtl.hello.mvc.dao;

import com.funtl.hello.mvc.entity.User;

public interface UserDao {
  User login(String loginId, String loginPwd);
}
