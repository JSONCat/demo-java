package com.funtl.my.shop.web.admin.service.impl;

import com.funtl.my.shop.domain.TbUser;
import com.funtl.my.shop.web.admin.dao.TbUserDao;
import com.funtl.my.shop.web.admin.service.TbUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/27
 */
@Service
public class TbUserServiceImpl implements TbUserService {
  @Autowired
  private TbUserDao tbUserDao;
  
  @Override
  public List<TbUser> selectAll() {
    return tbUserDao.selectAll();
  }
  
  @Override
  public void insert(TbUser tbUser) {
    tbUserDao.insert(tbUser);
  }
  
  @Override
  public void delete(Long id) {
    tbUserDao.delete(id);
  }
  
  @Override
  public TbUser getById(Long id) {
    return tbUserDao.getById(id);
  }
  
  @Override
  public void update(TbUser tbUser) {
    tbUserDao.update(tbUser);
  }
  
  @Override
  public List<TbUser> selectByName(String name) {
    return tbUserDao.selectByName(name);
  }
  
  @Override
  public TbUser login(String email, String password) {
    TbUser tbUser = tbUserDao.getByEmail(email);
    if (tbUser!=null) {
      String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
      if (tbUser.getPassword().equals(md5Password)) {
        return tbUser;
      }
    }
    return null;
  }
}
