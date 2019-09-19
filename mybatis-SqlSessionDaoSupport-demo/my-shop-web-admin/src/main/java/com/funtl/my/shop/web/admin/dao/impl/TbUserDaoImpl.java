package com.funtl.my.shop.web.admin.dao.impl;

import com.funtl.my.shop.commons.dao.baseSqlDao;
import com.funtl.my.shop.domain.TbUser;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
@Repository
public class TbUserDaoImpl extends baseSqlDao<TbUserDaoImpl> {


    //对studentMapper进行初始化的方法
    @PostConstruct
    public void init()
    {
        System.out.println("初始化studentMapper...");
        this.sqlDao = this.getSqlSession().getMapper(TbUserDaoImpl.class);
    }
    public List<TbUser> selectAll() {
        return null;
    }


    public int insert(TbUser tbUser) {
        int count = this.sqlDao.insert(tbUser);
        System.out.println("添加了" + count + "行数据。");
        return count;
    }


    public int delete(Long id) {
        int count = this.sqlDao.delete(id);
        System.out.println("删除了" + count + "行数据。");
        return count;
    }


    public TbUser getById(Long id) {
        return this.sqlDao.getById(id);
    }


    public int update(TbUser tbUser) {
        int count = this.sqlDao.update(tbUser);
        System.out.println("修改了" + count + "行数据。");
        return count;
    }


    public List<TbUser> selectByName(String name) {
        return null;
    }


    public TbUser getByEmail(String email) {
        return null;
    }
}
