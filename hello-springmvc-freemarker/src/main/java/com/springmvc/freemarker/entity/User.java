package com.springmvc.freemarker.entity;
import java.io.Serializable;
/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/31
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private String name;
    private Integer age;

    public User() {
        super();
    }
    public User(String name, Integer age) {
        super();
        this.name = name;
        this.age = age;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }

}
