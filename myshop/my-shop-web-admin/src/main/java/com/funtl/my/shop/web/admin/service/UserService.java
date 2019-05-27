package com.funtl.my.shop.web.admin.service;

import com.funtl.my.shop.domain.User;

public interface UserService {
  public User login(String email, String password);
}
