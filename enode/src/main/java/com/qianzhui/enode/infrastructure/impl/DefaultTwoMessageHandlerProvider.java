package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.common.container.IObjectContainer;
import com.qianzhui.enode.eventing.IDomainEvent;
import com.qianzhui.enode.infrastructure.IMessageHandlerProxy2;
import com.qianzhui.enode.infrastructure.ITwoMessageHandler;
import com.qianzhui.enode.infrastructure.ITwoMessageHandlerProvider;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class DefaultTwoMessageHandlerProvider extends AbstractHandlerProvider<ManyType, IMessageHandlerProxy2, List<Class>> implements ITwoMessageHandlerProvider {
    private IObjectContainer objectContainer;

    @Inject
    public DefaultTwoMessageHandlerProvider(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
    }

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
        return "handleAsync".equals(method.getName())
                && method.getParameterTypes().length == 2
                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[0])
                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[1]);
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }
}
