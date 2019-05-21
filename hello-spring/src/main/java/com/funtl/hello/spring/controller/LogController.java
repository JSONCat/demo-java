package com.funtl.hello.spring.controller;

import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.VariableElement;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/17
 */
public class LogController {
   public static final Logger logger = LoggerFactory.getLogger("com.Mylog");
   public static void main(String[] args) {
     logger.error(String.format("我是日志",":哈哈"));
   }
}
