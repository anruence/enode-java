package com.enodeframework.common.container;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring Ioc容器
 */

/**
 * @author anruence@gmail.com
 */
public class SpringObjectContainer implements IObjectContainer, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static <T> T resolveStatic(Class<T> targetClz) {
        return applicationContext.getBean(targetClz);
    }

    public static <T> T resolveStatic(String beanName, Class<T> targetClz) {
        return applicationContext.getBean(beanName, targetClz);
    }

    @Override
    public <TService> TService resolve(Class<TService> serviceType) {
        return applicationContext.getBean(serviceType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringObjectContainer.applicationContext = applicationContext;
    }
}
