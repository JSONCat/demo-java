package com.funtl.my.shop.web.admin.dao;

import com.funtl.my.shop.domain.TbUser;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/27
 */
@Repository
public interface TbUserDao{

    /**
     * 查询全部用户信息
     * @return
     */
     List<TbUser> selectAll();
    
    /**
     * 新增用户
     * @param tbUser
     */
     int insert(TbUser tbUser);
    
    /**
     * 删除用户
     * @param id
     */
    int delete(Long id);
    
    /**
     * 查询单个用户信息
     * @param id
     * @return
     */
    TbUser getById(Long id);
    
    /**
     * gen
     * @param tbUser
     * @return
     */
    int update(TbUser tbUser);
    
    /**
     * 根据用户名进行模糊查询
     * @param name
     * @return
     */
    List<TbUser> selectByName(String name);
    
    TbUser getByEmail(String email);
}
