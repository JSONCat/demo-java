package com.funtl.hello.beanname.viewresolver.view;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/31
 */
@Component
public class HelloView implements View {
    public String getContentType() {
        // TODO Auto-generated method stub
        return "text/html";
    }
  
  @Override
  public void render(Map<String, ?> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
      httpServletResponse.getWriter().print("Welcome to View:"+new Date());
  }
  
}