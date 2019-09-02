package com.enodeframework.messaging.impl;

import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.infrastructure.impl.AbstractHandlerProvider;
import com.enodeframework.infrastructure.impl.ManyType;
import com.enodeframework.messaging.IMessageHandlerProxy2;
import com.enodeframework.messaging.ITwoMessageHandlerProvider;
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
        return isMethodAnnotationSubscribe(method);
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }

    public DefaultTwoMessageHandlerProvider setObjectContainer(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
        return this;
    }
}
