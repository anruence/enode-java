package com.enodeframework.common.container;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Spring Ioc容器
 *
 * @author anruence@gmail.com
 */
public class SpringObjectContainer implements IObjectContainer, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public <T> Map<String, T> resolveAll(Class<T> targetClz) {
        return applicationContext.getBeansOfType(targetClz);
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
