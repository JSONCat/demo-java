package com.funtl.my.shop.commons.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * spring 配置类
 *
 * @author wuyiwen
 * @createDate 2019/5/21
 */
public final class SpringContext {
  public static ApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
}
