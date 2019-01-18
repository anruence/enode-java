package com.enode.infrastructure.impl;

import com.enode.common.container.IObjectContainer;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageHandler;
import com.enode.infrastructure.IMessageHandlerProvider;
import com.enode.infrastructure.IMessageHandlerProxy1;

import javax.inject.Inject;
import java.lang.reflect.Method;

public class DefaultMessageHandlerProvider extends AbstractHandlerProvider<Class, IMessageHandlerProxy1, Class> implements IMessageHandlerProvider {
    private IObjectContainer objectContainer;

    @Inject
    public DefaultMessageHandlerProvider(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
    }

    @Override
    protected Class getGenericHandlerType() {
        return IMessageHandler.class;
    }

    @Override
    protected Class getKey(Method method) {
        return method.getParameterTypes()[0];
    }

    @Override
    protected Class<? extends IMessageHandlerProxy1> getHandlerProxyImplementationType() {
        return MessageHandlerProxy1.class;
    }

    @Override
    protected boolean isHandlerSourceMatchKey(Class handlerSource, Class key) {
        return key.isAssignableFrom(handlerSource);
    }

    @Override
    protected boolean isHandleMethodMatch(Method method) {
        if (!"handleAsync".equals(method.getName())) {
            return false;
        }
        if (method.getParameterTypes().length != 1) {
            return false;
        }
        if (IMessage.class.equals(method.getParameterTypes()[0])) {
            return false;
        }
        if (!IMessage.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return false;
        }
        return true;
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }
}
