package com.funtl.my.shop.web.controller;

import com.funtl.my.shop.commons.context.SpringContext;
import com.funtl.my.shop.entity.User;
import com.funtl.my.shop.service.UserService;

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
 
  private UserService userService =  SpringContext.getBean("userService");
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
  
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String email = req.getParameter("email");
    String password = req.getParameter("password");
    User user = userService.login(email, password);
    if (user != null) {
      resp.sendRedirect("/main.jsp");
    } else {
      req.setAttribute("error","用户名或密码错误");
      req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
    
  }
}
