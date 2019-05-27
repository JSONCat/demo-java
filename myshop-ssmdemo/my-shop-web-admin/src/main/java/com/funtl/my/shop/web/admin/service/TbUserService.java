package com.funtl.my.shop.web.admin.service;

import com.funtl.my.shop.domain.TbUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/27
 */

public interface TbUserService {

    /**
     * 查询全部用户信息
     * @return
     */
    List<TbUser> selectAll();
    /**
     * 新增用户
     * @param tbUser
     */
    void insert(TbUser tbUser);
    
    /**
     * 删除用户
     * @param id
     */
    void delete(Long id);
    
    /**
     * 查询单个用户信息
     * @param id
     * @return
     */
    TbUser getById(Long id);
    
    /**
     * 更新用户信息
     * @param tbUser
     * @return
     */
    void update(TbUser tbUser);
    
    /**
     * 根据用户名进行模糊查询
     * @param name
     * @return
     */
    List<TbUser> selectByName(String name);
    
    /**
     * 登录
     * @param email
     * @param password
     * @return
     */
    TbUser login(String email, String password);
    
    
}