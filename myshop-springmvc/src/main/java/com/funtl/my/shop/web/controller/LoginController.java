package com.funtl.my.shop.web.controller;

import com.funtl.my.shop.commons.context.SpringContext;
import com.funtl.my.shop.commons.utils.CookieUtils;
import com.funtl.my.shop.entity.User;
import com.funtl.my.shop.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.funtl.my.shop.commons.constant.ConstantUtils.SESSION_USER;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
@Controller
public class LoginController {
  final static String COOKIE_NAME_USER_INFO = "userinfo";
  @Autowired
  private UserService userService;
  
  @RequestMapping(value = {"/","/login"}, method = RequestMethod.GET)
  public String login(HttpServletRequest request){
    return "login";
  }
  @RequestMapping(value = "aa", method = RequestMethod.GET)
  public String aa(HttpServletRequest request){
   
    
    return "a";
  }
 
  
  
  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public String login(@RequestParam(required = true) String email, @RequestParam(required = true)String password, HttpServletRequest request){
    User user = userService.login(email, password);
    if (user == null) {
      return login(request);
    } else {
      request.getSession().setAttribute(SESSION_USER,user);
      return "redirect:/main";
    }
   
  }

  
  
  
}
