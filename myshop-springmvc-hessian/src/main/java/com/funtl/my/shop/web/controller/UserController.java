package com.funtl.my.shop.web.controller;

import com.funtl.my.shop.service.HessianService;
import com.funtl.my.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
@Controller
@RequestMapping("/user")
public class UserController {
  @Autowired
  private UserService userService;
  @Autowired
  private HessianService hessianService;
  
  
  @RequestMapping(value = "/add")
  @ResponseBody
  public String addUser(){
    hessianService.notice("新增用户成功");
    return "json";
  }
  
}
