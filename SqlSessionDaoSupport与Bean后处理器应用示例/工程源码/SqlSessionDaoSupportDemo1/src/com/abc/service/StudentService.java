package com.abc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abc.dao.StudentDao;
import com.abc.domain.Student;

@Service
public class StudentService {
	
	@Autowired
	private StudentDao studentDao;
	//根据id查找学生
	public Student getById(int id)
	{
		return this.studentDao.getById(id);
	}
	
	//修改学生信息
	public void update(Student student)
	{
		this.studentDao.update(student);
	}
	
	public void add(Student student)
	{
		this.studentDao.add(student);
		
	}
}
