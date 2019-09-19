package com.funtl.my.shop.web.admin.web.controller;

import com.funtl.my.shop.commons.dto.BaseResult;
import com.funtl.my.shop.domain.TbUser;
import com.funtl.my.shop.web.admin.service.TbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/27
 */
@Controller
@RequestMapping("/user")
public class UserController {
  @Autowired
  private TbUserService tbUserService;
  @RequestMapping(value = "/list", method = RequestMethod.GET)
  public String list(ModelMap modelMap){
    List<TbUser> tbUsers = tbUserService.selectAll();
    modelMap.addAttribute("tbUsers",tbUsers);
    return "user_list";
  }

  @RequestMapping(value = "/form", method = RequestMethod.GET)
  public String form(ModelMap modelMap){
    return "user_form";
  }


  @RequestMapping(value = "/save", method = RequestMethod.POST)
  public String save(TbUser aa, Model model, RedirectAttributes redirectAttributes){
    BaseResult baseResult = BaseResult.fail("失败");

    // 保存成功
    if (baseResult.getStatus() == 200) {
      redirectAttributes.addFlashAttribute("baseResult", baseResult);
      return "redirect:/user/list";
    }

    // 保存失败
    else {
      model.addAttribute("baseResult", baseResult);
      return "user_form";
    }
  }

  /**
   * 测试页
   * @return
   */
  @RequestMapping(value = "add")
  public String add(ModelMap modelMap){
    TbUser tbUser = new TbUser();
    tbUser.setUsername("我是测试用户");
    tbUser.setEmail("test@qq.com");
    modelMap.addAttribute("user",tbUser);
    return "json";
  }

}
