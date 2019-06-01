package com.funtl.hello.beanname.viewresolver.controller;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/31
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

@RequestMapping(value="/springmvc")
@Controller
public class BeanNameController {
    
    @RequestMapping(value="/testMyView")
    public String testView(){
        System.out.println("testView");
        return "helloView";
    }
}