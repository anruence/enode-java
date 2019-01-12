package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.common.container.IObjectContainer;
import com.qianzhui.enode.eventing.IDomainEvent;
import com.qianzhui.enode.infrastructure.IMessageHandler;
import com.qianzhui.enode.infrastructure.IMessageHandlerProxy3;
import com.qianzhui.enode.infrastructure.IThreeMessageHandlerProvider;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class DefaultThreeMessageHandlerProvider extends AbstractHandlerProvider<ManyType, IMessageHandlerProxy3, List<Class>> implements IThreeMessageHandlerProvider {
    private IObjectContainer objectContainer;

    @Inject
    public DefaultThreeMessageHandlerProvider(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
    }

    @Override
    protected Class getGenericHandlerType() {
        return IMessageHandler.class;
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
        return "handleAsync".equals(method.getName())
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
