package com.funtl.my.shop.service.impl;

import com.funtl.my.shop.service.HessianService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 此处填写说明
 *
 * @author wuyiwen
 * @createDate 2019/5/31
 */
public class HessianClientServiceImpl implements HessianService {
  
  
  @Resource(name="sasHessianList")
	private List<HessianService> sasHessianList;
  
  /**
   * 通知所有服务器
   * @param message
   */
  @Override
  public void notice(String message) {
		for(final HessianService hessian : sasHessianList){
			hessian.notice("新增用户成功！");
		}
  }
  
  /***************
	 * 获取所有hessian
	 * @return
	 */
	public List<HessianService> getHessianServiceList()
	{
	  return this.sasHessianList;
		
	}
	

}
