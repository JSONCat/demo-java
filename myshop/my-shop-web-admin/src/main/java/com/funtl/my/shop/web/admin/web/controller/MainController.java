package com.funtl.my.shop.web.admin.web.controller;

import com.funtl.my.shop.web.admin.service.TbUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
@Controller
@RequestMapping("/main")
public class MainController {
  @Autowired
  private TbUserService tbUserService;
  
  @RequestMapping(value = "", method = RequestMethod.GET)
  public String main(){
    
    return "main";
  }
  
}
