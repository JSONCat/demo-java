package com.funtl.my.shop.web.admin.web.controller;

import com.funtl.my.shop.domain.TbUser;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/27
 */
public class UserController {
  /**
   * 测试页
   * @return
   */
  @RequestMapping(value = "json", method = RequestMethod.GET)
  public String login(ModelMap modelMap){
    TbUser tbUser = new TbUser();
    tbUser.setUsername("我是测试用户");
    tbUser.setEmail("test@qq.com");
    modelMap.addAttribute("user",tbUser);
    return "json";
  }
}
