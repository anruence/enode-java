package com.enode.common.container;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringObjectContainer implements IObjectContainer, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public <TService> TService resolve(Class<TService> serviceType) {
        return applicationContext.getBean(serviceType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
