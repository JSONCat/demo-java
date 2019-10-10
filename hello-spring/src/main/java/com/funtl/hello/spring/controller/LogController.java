package com.funtl.hello.spring.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/17
 */
public class LogController {

    public static final Logger logger = LoggerFactory.getLogger(LogController.class);
   public static void main(String[] args) {

       System.out.println(String.format("我是日志",":哈哈"));
   }
}
