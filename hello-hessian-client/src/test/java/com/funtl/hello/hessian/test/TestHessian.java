package com.funtl.hello.hessian.test;


import com.funtl.hello.hessian.service.ClientUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/30
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-context.xml")
public class TestHessian {
  @Autowired
	private ClientUserService clientUserService;
	@Test
	public void Test() {
		this.clientUserService.addUser();
	}

}
