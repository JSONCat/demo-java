package com.funtl.hello.spring.service.test;

import com.funtl.hello.spring.service.UserSetvice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/17
 */
public class UserServiceTest {
  
  private UserSetvice userSetvice;
  @Before
  public void before(){
    System.out.println("初始化数据连接");
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
    userSetvice = (UserSetvice) context.getBean("useService");
    
  }
  
  @Test
  public void testSayHi(){
    userSetvice.sayHi();
  }
  
  @Test
  public void testHello(){
    System.out.println("Hello Junit");
  }
  
  
  /**
   * 测试断言
   */
  @Test
  public void testAssert() {
      String obj1 = "junit";
      String obj2 = "junit";
      String obj3 = "test";
      String obj4 = "test";
      String obj5 = null;
      int var1 = 1;
      int var2 = 2;
      int[] arithmetic1 = {1, 2, 3};
      int[] arithmetic2 = {1, 2, 3};
      //断言值相等
      assertEquals(obj1, obj2);
      //断言引用同个对象
      assertSame(obj3, obj4);
      //断言不是同个对象
      assertNotSame(obj2, obj4);
      //断言非null
      assertNotNull(obj1);
      //断言null
      assertNull(obj5);
      //断言true
      assertTrue("为真", var1 == var2);
  
      assertArrayEquals(arithmetic1, arithmetic2);
  }
  
  @After
  public void after(){
    System.out.println("断开数据连接");
  }
}
