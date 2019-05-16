package com.funtl.hello.mvc.service;

import com.funtl.hello.mvc.entity.User;

public interface UserService {
  User login (String loginId, String loginPwd);
}
