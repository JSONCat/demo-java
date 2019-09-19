package com.funtl.my.shop.commons.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
public abstract class baseSqlDao<T> extends SqlSessionDaoSupport {
    public T sqlDao;
    @Autowired
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate)
    {
        super.setSqlSessionTemplate(sqlSessionTemplate);
    }
    //抽象方法
    public abstract void init();

}