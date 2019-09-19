package com.demo;

import org.springframework.context.ApplicationContext;
import com.abc.service.StudentService;
import com.abc.domain.Student;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestDaoSupport {
	private static ApplicationContext ctx;
	static {
		// 在类路径下寻找spring主配置文件，启动spring容器
		ctx = new ClassPathXmlApplicationContext(
				"classpath:/applicationContext.xml");
	}

	public static void main(String[] args) {
		// 从Spring容器中请求服务组件
		StudentService studentService = 
				(StudentService)ctx.getBean("studentService");
		
		Student student = studentService.getById(13);
		System.out.println(student.getName());
		
	}

}