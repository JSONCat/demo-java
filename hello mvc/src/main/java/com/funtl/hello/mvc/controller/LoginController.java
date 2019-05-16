package com.funtl.hello.mvc.controller;

import com.funtl.hello.mvc.entity.User;
import com.funtl.hello.mvc.service.UserService;
import com.funtl.hello.mvc.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/16
 */
public class LoginController extends HttpServlet {
  
  private UserService userService = new UserServiceImpl();
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("11");
    req.getRequestDispatcher("index.jsp").forward(req,resp);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String loginId = req.getParameter("loginId");
    String loginPwd = req.getParameter("loginPwd");
    
    User user = userService.login(loginId, loginPwd);
    if (user == null) {
      req.getRequestDispatcher("fail.jsp").forward(req,resp);
    } else {
      req.getRequestDispatcher("success.jsp").forward(req,resp);
    }
    
  }
}
