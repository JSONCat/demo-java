package com.funtl.my.shop.web.admin.web.controller;


import com.funtl.my.shop.domain.User;
import com.funtl.my.shop.web.admin.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
  
  /**
   * 登录页
   * @return
   */
  @RequestMapping(value = {"/","/login"}, method = RequestMethod.GET)
  public String login(){
    return "login";
  }
 
  
  
  /**
   * 登录action
   * @param email
   * @param password
   * @param request
   * @return
   */
  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public String login(@RequestParam(required = true) String email, @RequestParam(required = true)String password, HttpServletRequest request){
    User user = userService.login(email, password);
    if (user == null) {
      return login();
    } else {
      request.getSession().setAttribute(SESSION_USER,user);
      return "redirect:/main";
    }
   
  }
  
  /**
   * 注销
   *
   * @param httpServletRequest
   * @return
   */
  @RequestMapping(value = "logout", method = RequestMethod.GET)
  public String logout(HttpServletRequest httpServletRequest) {
      httpServletRequest.getSession().invalidate();
      return login();
  }

  
  
  
}
