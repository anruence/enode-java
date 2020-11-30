package org.enodeframework.messaging.impl;

import kotlin.coroutines.Continuation;
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
public class DefaultTwoMessageHandlerProvider extends AbstractHandlerProvider<ManyType, IMessageHandlerProxy2, List<Class<?>>> implements ITwoMessageHandlerProvider {

    @Override
    protected ManyType getKey(Method method) {
        return new ManyType(Arrays.asList(method.getParameterTypes()));
    }

    @Override
    protected Class<? extends IMessageHandlerProxy2> getHandlerProxyImplementationType() {
        return MessageHandlerProxy2.class;
    }

    @Override
    protected boolean isHandlerSourceMatchKey(List<Class<?>> handlerSource, ManyType key) {
        for (Class<?> type : key.getTypes()) {
            if (!handlerSource.stream().anyMatch(x -> x == type)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean isHandleMethodMatch(Method method) {
        int paramCount = method.getParameterTypes().length;
        if (paramCount != 2) {
            if (!isSuspendMethod(method)) {
                return false;
            }
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
    protected boolean isSuspendMethod(Method method) {
        return method.getParameterTypes().length == 3 && Continuation.class.equals(method.getParameterTypes()[2]);
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return ObjectContainer.INSTANCE;
    }

}
