package com.springmvc.freemarker.controller;

import com.springmvc.freemarker.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/31
 */
@Controller
@RequestMapping(value = "/user")
public class UserController {

    @RequestMapping(value = "/info")
    public String testFreemarker(HttpServletRequest request, ModelMap modelMap) throws Exception {
        User user = new User("may", 21);
        
        List<String> list = new ArrayList<String>();
        list.add("Jack1");
        list.add("Jack2");
        list.add("Jack3");
        
        modelMap.addAttribute("list", list);
        modelMap.addAttribute("user", user);
        request.setAttribute("haha","哈哈");
        return "userinfo";
    }

}