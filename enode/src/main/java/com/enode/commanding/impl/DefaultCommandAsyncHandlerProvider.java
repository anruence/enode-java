package com.enode.commanding.impl;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandAsyncHandler;
import com.enode.commanding.ICommandAsyncHandlerProvider;
import com.enode.commanding.ICommandAsyncHandlerProxy;
import com.enode.common.container.IObjectContainer;
import com.enode.infrastructure.impl.AbstractHandlerProvider;

import javax.inject.Inject;
import java.lang.reflect.Method;

public class DefaultCommandAsyncHandlerProvider extends AbstractHandlerProvider<Class, ICommandAsyncHandlerProxy, Class> implements ICommandAsyncHandlerProvider {
    private IObjectContainer objectContainer;

    @Inject
    public DefaultCommandAsyncHandlerProvider(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
    }

    @Override
    protected Class getGenericHandlerType() {
        return ICommandAsyncHandler.class;
    }

    @Override
    protected Class getKey(Method method) {
        return method.getParameterTypes()[0];
    }

    @Override
    protected Class<? extends ICommandAsyncHandlerProxy> getHandlerProxyImplementationType() {
        return CommandAsyncHandlerProxy.class;
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
        if (!ICommand.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return false;
        }
        return true;
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }
}
