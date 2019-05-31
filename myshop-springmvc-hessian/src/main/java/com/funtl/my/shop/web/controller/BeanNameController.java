package com.funtl.my.shop.web.controller;

import com.funtl.my.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/31
 */
@Controller
public class BeanNameController {


  
  @RequestMapping(value = {"/","/home"}, method = RequestMethod.GET)
  public String login(HttpServletRequest request){
    return "home";
  }
 
  

}
