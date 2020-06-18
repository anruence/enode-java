package org.enodeframework.messaging.impl;

import org.enodeframework.common.container.IObjectContainer;
import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider;
import org.enodeframework.infrastructure.impl.ManyType;
import org.enodeframework.messaging.IMessageHandlerProxy2;
import org.enodeframework.messaging.ITwoMessageHandlerProvider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class DefaultTwoMessageHandlerProvider extends AbstractHandlerProvider<ManyType, IMessageHandlerProxy2, List<Class>> implements ITwoMessageHandlerProvider {

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
        return ObjectContainer.INSTANCE;
    }

}
