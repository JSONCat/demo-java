package com.funtl.my.shop.web.admin.web.interceptor;

import com.funtl.my.shop.domain.TbUser;
import com.funtl.my.shop.domain.User;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.funtl.my.shop.commons.constant.ConstantUtils.SESSION_USER;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/24
 */
public class PermissionInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
    return true;
  }
  
  @Override
  public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    String method = httpServletRequest.getMethod();
    if ("GET".equals(method)) {
      TbUser tbUser = (TbUser)httpServletRequest.getSession().getAttribute(SESSION_USER);
      if (tbUser != null){
        httpServletResponse.sendRedirect("/main");
      }
    }
  }
  
  @Override
  public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
  
  }
}
