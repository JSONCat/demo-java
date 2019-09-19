package com.sas.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.ReflectionUtils;

/**
 * 
 * 此类目的扩展PropertyPlaceholderConfigurer，使支持properties属性值支持annotation注入
 * 
 * <li>注：AnnotationProperty标签默认值是:bean.getClass().getName() + "." + field.getName();
 * <li>当获取不到properties值时，或值为空串时，将跳过不处理
 * 
 * <pre>
 *   public class MetaClass{
 *      &#064;AnnotationProperty(&quot;dbKey&quot;) private String dbKey;
 *      &#064;AnnotationProperty(&quot;userId&quot;) private long userId;
 *      
 *      ....//setter|getter
 *   }
 *   
 *   xml配置如下：
 *      &lt;bean id=&quot;propertyConfigurer&quot;
 *         class=&quot;com.netease.mylife.base.factory.spring.AnnotationPropertyConfigurer&quot;&gt;
 *         &lt;property name=&quot;locations&quot;&gt;
 *             &lt;list&gt;
 *                 &lt;value&gt;classpath:db.properties&lt;/value&gt;
 *                 &lt;value&gt;classpath:mylife.properties&lt;/value&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 *     
 *   db.properties内容：
 *   dbKey = stringdbkey
 *   
 *   mylife.properties:内容：
 *   userId = 770822
 * </pre>
 * 
 * @author caihui@163.org
 * 
 */
public class AnnotationPropertyConfigurer extends PropertyPlaceholderConfigurer implements BeanPostProcessor, InitializingBean {

    @Target( { ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface AnnotationProperty {
        String value() default "";
    }

    protected static transient Logger logger = Logger.getLogger(AnnotationPropertyConfigurer.class);
    private Properties props;

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        List<Field> fields = drawoffFields(bean.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(AnnotationProperty.class)) {
                AnnotationProperty p = field.getAnnotation(AnnotationProperty.class);
                if (p == null) {
                    continue;// 不可能吧!!!!!，肯定出错了
                }
                try {
                    String key = p.value();
                    if(StringUtils.isBlank(key)) {
                        key = bean.getClass().getName() + "." + field.getName();
                    }
                    String value = props.getProperty(key);
                    if(StringUtils.isBlank(value)) {
                        continue;//不可能吧!!!!!，肯定出错了 
                    }
                    ReflectionUtils.makeAccessible(field);
                    field.set(bean, ConvertUtils.convert(value, field.getType()));
                } catch (Exception e) {
                    throw new FatalBeanException("属性注入出错，请检查：" + bean.getClass() + "." + field.getName() + "是否正确");
                }
            }
        }
        return bean;
    }
    
    private static List<Field> drawoffFields(Class<?> clz){
        List<Field> fields = new LinkedList<Field>();
        if(clz == null) {
            return fields;
        }
        fields.addAll(drawoffFields(clz.getSuperclass()));
        fields.addAll(Arrays.asList(clz.getDeclaredFields()));
        return fields;
    }

    public void afterPropertiesSet() throws Exception {
        props = mergeProperties();
    }
}
