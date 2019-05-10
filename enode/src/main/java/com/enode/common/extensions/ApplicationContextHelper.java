package com.enode.common.extensions;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static <T> T getBean(Class<T> targetClz) {
        return applicationContext.getBean(targetClz);
    }

    public static <T> T getBean(String beanName, Class<T> targetClz) {
        return applicationContext.getBean(beanName, targetClz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHelper.applicationContext = applicationContext;
    }
}
