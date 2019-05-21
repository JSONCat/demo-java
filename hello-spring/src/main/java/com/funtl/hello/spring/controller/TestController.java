package com.funtl.hello.spring.controller;

import com.funtl.hello.spring.service.UserSetvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/17
 */
public class TestController {
 
  public static final Logger logger = LoggerFactory.getLogger(TestController.class);
  public static void main(String[] args) {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-context.xml");
    UserSetvice useService = (UserSetvice) applicationContext.getBean("useService");
    useService.sayHi();
    
     logger.info("slf4j for info");
      logger.debug("slf4j for debug");
      logger.error("slf4j for error");
      logger.warn("slf4j for warn");

      String message = "Hello SLF4J";
      logger.info("slf4j message is : {}", message);
  }
}
