package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.infrastructure.IMessageHandlerProxy2;
import com.enodeframework.infrastructure.ITwoMessageHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class DefaultTwoMessageHandlerProvider extends AbstractHandlerProvider<ManyType, IMessageHandlerProxy2, List<Class>> implements ITwoMessageHandlerProvider {

    @Autowired
    private IObjectContainer objectContainer;

    @Override
    protected ManyType getKey(Method method) {
        return new ManyType(Arrays.asList(method.getParameterTypes()));
    }

    @Override
    protected Class<? extends IMessageHandlerProxy2> getHandlerProxyImplementationType() {
        return MessageHandlerProxy2.class;
    }

    @Override
    protected boolean isHandlerSourceMatchKey(List<Class> handlerSource, ManyType key) {
        if (handlerSource.size() != 2) {
            return false;
        }

        for (Class type : key.getTypes()) {
            if (!handlerSource.stream().anyMatch(x -> x == type)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean isHandleMethodMatch(Method method) {
        if (method.getParameterTypes().length != 2) {
            return false;
        }
        if (!IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return false;
        }
        if (!IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[1])) {
            return false;
        }
        if (!isMethodAnnotationSubscribe(method)) {
            return false;
        }
        return true;
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }
}
