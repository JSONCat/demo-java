package com.abc.mapper;

import com.abc.domain.Student;

public interface StudentMapper {
		
	//根据id查找学生
	public Student getById(int id);
	
	//添加一名学生
	public int add(Student student);
	
	//修改学生
	public int update(Student student);
	
	//删除学生
	public int delete(int id);
	
}
