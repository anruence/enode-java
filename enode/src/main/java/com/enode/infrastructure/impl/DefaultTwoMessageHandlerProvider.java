package com.enode.infrastructure.impl;

import com.enode.common.Constants;
import com.enode.common.container.IObjectContainer;
import com.enode.eventing.IDomainEvent;
import com.enode.infrastructure.IMessageHandlerProxy2;
import com.enode.infrastructure.ITwoMessageHandler;
import com.enode.infrastructure.ITwoMessageHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class DefaultTwoMessageHandlerProvider extends AbstractHandlerProvider<ManyType, IMessageHandlerProxy2, List<Class>> implements ITwoMessageHandlerProvider {

    @Autowired
    private IObjectContainer objectContainer;

    @Override
    protected Class getGenericHandlerType() {
        return ITwoMessageHandler.class;
    }

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
        return Constants.HANDLE_METHOD.equals(method.getName())
                && method.getParameterTypes().length == 2
                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[0])
                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[1]);
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }
}
