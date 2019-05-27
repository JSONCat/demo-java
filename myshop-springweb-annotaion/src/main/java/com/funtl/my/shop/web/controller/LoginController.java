package com.funtl.my.shop.web.controller;

import com.funtl.my.shop.commons.context.SpringContext;
import com.funtl.my.shop.commons.utils.CookieUtils;
import com.funtl.my.shop.entity.User;
import com.funtl.my.shop.service.UserService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
public class LoginController extends HttpServlet {
  final static String COOKIE_NAME_USER_INFO = "userinfo";
  
  private UserService userService =  SpringContext.getBean("userService");
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    String userinfo = CookieUtils.getCookieValue(req, COOKIE_NAME_USER_INFO);
    
    if (StringUtils.isNoneBlank(userinfo)){
      String[] array = userinfo.split("_");
      if (array.length==2) {
        String email = array[0];
        String password = array[1];
        req.setAttribute("email",email);
        req.setAttribute("password",password);
        req.setAttribute("isRemenber",true);
      }
    }
    req.getRequestDispatcher("/login.jsp").forward(req,resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String email = req.getParameter("email");
    String password = req.getParameter("password");
    boolean isRemenber = "on".equals(req.getParameter("isRemenber")) ? true : false;
    
    if (!isRemenber) {
      CookieUtils.deleteCookie(req,resp,COOKIE_NAME_USER_INFO);
    }
    User user = userService.login(email, password);
    if (user != null) {
      if (isRemenber) {
        //用户信息存储一周
        CookieUtils.setCookie(req,resp,COOKIE_NAME_USER_INFO,String.format("%s_%s",email,password), 60*60*24*7 );
      }
      resp.sendRedirect("/main.jsp");
    } else {
      req.setAttribute("error","用户名或密码错误");
      req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
    
  }
}
