package com.funtl.my.shop.service;

import com.funtl.my.shop.entity.User;

public interface UserService {
  public User login(String email, String password);
}
