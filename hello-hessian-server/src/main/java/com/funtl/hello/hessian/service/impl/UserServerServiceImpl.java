package com.funtl.hello.hessian.service.impl;

import com.funtl.hello.hessian.service.UserServerService;


/**
 * 描述： 作为hessian服务端的 要提供的接口的实现类
 *
 * @author 80003071
 *
 */
public class UserServerServiceImpl implements UserServerService {
 
	@Override
	public void addUser() {
		System.out.println("-----------调用了实现类的 addUser()方法-------------");
	}
 
	@Override
	public void updateUser() {
		System.out.println("-----------调用了实现类的 updateUser()方法-------------");
	}
 
	@Override
	public void deleteUser() {
		System.out.println("-----------调用了实现类的 deleteUser()方法-------------");
	}
 
}
