package com.abc.dao.base;

import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class BaseDao extends SqlSessionDaoSupport {

	@Autowired
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate)
    {
    	super.setSqlSessionTemplate(sqlSessionTemplate);
    }
	
	//抽象方法
	public abstract void init(); 
	
}
