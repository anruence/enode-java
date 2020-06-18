package org.enodeframework.spring;

import org.enodeframework.common.container.IObjectContainer;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Spring Ioc容器
 *
 * @author anruence@gmail.com
 */
public class SpringObjectContainer implements IObjectContainer {

    private final ApplicationContext applicationContext;

    public SpringObjectContainer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> Map<String, T> resolveAll(Class<T> targetClz) {
        return applicationContext.getBeansOfType(targetClz);
    }

    @Override
    public <TService> TService resolve(Class<TService> serviceType) {
        return applicationContext.getBean(serviceType);
    }

}
