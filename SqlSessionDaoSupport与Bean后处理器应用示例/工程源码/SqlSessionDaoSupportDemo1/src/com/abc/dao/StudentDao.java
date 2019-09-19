package com.abc.dao;

import org.springframework.stereotype.Repository;

import com.abc.dao.base.BaseDao;
import com.abc.domain.Student;
import com.abc.mapper.StudentMapper;

@Repository
public class StudentDao extends BaseDao{
    private StudentMapper studentMapper;
	
	public Student getById(int id)
	{
		return this.studentMapper.getById(id);
	}
	
	public void deleteById(int id)
	{
		int count = this.studentMapper.delete(id);
		System.out.println("删除了" + count + "行数据。");
	}
	
	public void update(Student student)
	{
		int count = this.studentMapper.update(student);
		System.out.println("修改了" + count + "行数据。");
	}

	public void add(Student student) {
		// TODO Auto-generated method stub
		int count = this.studentMapper.add(student);
		System.out.println("添加了" + count + "行数据。");
	}

	//对studentMapper进行初始化的方法
	@Override
	public void init()
	{
		System.out.println("初始化studentMapper...");
		this.studentMapper 
		     = this.getSqlSession().getMapper(StudentMapper.class);
	}
	
}
