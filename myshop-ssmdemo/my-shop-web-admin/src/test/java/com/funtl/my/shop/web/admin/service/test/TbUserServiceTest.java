package com.funtl.my.shop.web.admin.service.test;

import com.funtl.my.shop.domain.TbUser;
import com.funtl.my.shop.web.admin.service.TbUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/27
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring-context.xml","classpath:spring-context-druid.xml","classpath:spring-context-mybatis.xml"})
public class TbUserServiceTest {
  @Autowired
  private TbUserService tbUserService;
  @Test
  public void testSelectAll(){
    List<TbUser> tbUsers = tbUserService.selectAll();
    for (TbUser tbUser : tbUsers){
      tbUser.toString();
      System.out.println(tbUser);
    }
  }
  
  @Test
  public void testInsert() {
      TbUser tbUser = new TbUser();
      tbUser.setEmail("admin@admin.com");
      tbUser.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
      tbUser.setPhone("15888888888");
      tbUser.setUsername("Lusifer");
      tbUser.setCreated(new Date());
      tbUser.setUpdated(new Date());
  
      tbUserService.insert(tbUser);
  }
  
  @Test
  public void testUpdate() {
      TbUser tbUser = tbUserService.getById(36L);
      tbUser.setUsername("test02");
  
      tbUserService.update(tbUser);
  }
  @Test
  public void testGetById() {
      TbUser tbUser = tbUserService.getById(36L);
      System.out.println(tbUser.getUsername());
  }
  @Test
  public void testSelectByName() {
    List<TbUser> tbUsers = tbUserService.selectByName("uni");
    for (TbUser tbUser : tbUsers) {
        System.out.println(tbUser.getUsername());
    }
}
  
  
}
