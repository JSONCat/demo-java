package com.funtl.my.shop.commons.context;

import com.funtl.my.shop.commons.dao.baseSqlDao;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // TODO Auto-generated method stub
        //只处理BaseDao的子类的对象
        if(bean.getClass().getSuperclass() == baseSqlDao.class)
        {
            baseSqlDao dao = (baseSqlDao)bean;
            dao.init();
        }
        //返回原bean实例
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // TODO Auto-generated method stub
        //直接返回原bean实例，不做任何处理
        return bean;
    }
}
