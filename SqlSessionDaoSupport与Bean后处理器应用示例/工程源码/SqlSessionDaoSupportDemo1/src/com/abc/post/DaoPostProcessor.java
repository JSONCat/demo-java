package com.abc.post;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import com.abc.dao.base.BaseDao;

public class DaoPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		// TODO Auto-generated method stub
		//只处理BaseDao的子类的对象
		if(bean.getClass().getSuperclass()==BaseDao.class)
		{
			BaseDao dao = (BaseDao)bean;
			dao.init();
		}
		//返回原bean实例
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		// TODO Auto-generated method stub
		//直接返回原bean实例，不做任何处理
		return bean;
	}

}
