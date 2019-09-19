package com.abc.domain;

public class Student {

	private int id;
	private String name;   //姓名
	private String gender; //性别
	private String major;  //专业
	private String grade;  //年级
	private Teacher supervisor; //指导教师
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getMajor() {
		return major;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}

	public Teacher getSupervisor()
	{
		return this.supervisor;
	}

	public void setSupervisor(Teacher supervisor)
	{
		this.supervisor = supervisor;
	}
	
}
