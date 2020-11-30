package org.enodeframework.messaging.impl;

import kotlin.coroutines.Continuation;
import org.enodeframework.common.container.IObjectContainer;
import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider;
import org.enodeframework.infrastructure.impl.ManyType;
import org.enodeframework.messaging.IMessageHandlerProxy3;
import org.enodeframework.messaging.IThreeMessageHandlerProvider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class DefaultThreeMessageHandlerProvider extends AbstractHandlerProvider<ManyType, IMessageHandlerProxy3, List<Class<?>>> implements IThreeMessageHandlerProvider {

    @Override
    protected ManyType getKey(Method method) {
        return new ManyType(Arrays.asList(method.getParameterTypes()));
    }

    @Override
    protected Class<? extends IMessageHandlerProxy3> getHandlerProxyImplementationType() {
        return MessageHandlerProxy3.class;
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
        if (paramCount != 3) {
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
        if (!IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[2])) {
            return false;
        }
        return isMethodAnnotationSubscribe(method);
    }

    @Override
    protected boolean isSuspendMethod(Method method) {
        return method.getParameterTypes().length == 4 && Continuation.class.equals(method.getParameterTypes()[3]);
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return ObjectContainer.INSTANCE;
    }

}
