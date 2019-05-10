package com.enode.infrastructure.impl;

import com.enode.common.Constants;
import com.enode.common.container.IObjectContainer;
import com.enode.eventing.IDomainEvent;
import com.enode.infrastructure.IMessageHandlerProxy3;
import com.enode.infrastructure.IThreeMessageHandler;
import com.enode.infrastructure.IThreeMessageHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class DefaultThreeMessageHandlerProvider extends AbstractHandlerProvider<ManyType, IMessageHandlerProxy3, List<Class>> implements IThreeMessageHandlerProvider {

    @Autowired
    private IObjectContainer objectContainer;

    @Override
    protected Class getGenericHandlerType() {
        return IThreeMessageHandler.class;
    }

    @Override
    protected ManyType getKey(Method method) {
        return new ManyType(Arrays.asList(method.getParameterTypes()));
    }

    @Override
    protected Class<? extends IMessageHandlerProxy3> getHandlerProxyImplementationType() {
        return MessageHandlerProxy3.class;
    }

    @Override
    protected boolean isHandlerSourceMatchKey(List<Class> handlerSource, ManyType key) {
        if (handlerSource.size() != 3) {
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
        return Constants.HANDLE_METHOD.equals(method.getName())
                && method.getParameterTypes().length == 3
                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[0])
                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[1])
                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[2]);
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }
}
