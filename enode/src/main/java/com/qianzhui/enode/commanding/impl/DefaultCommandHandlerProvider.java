package com.qianzhui.enode.commanding.impl;

import com.qianzhui.enode.commanding.ICommand;
import com.qianzhui.enode.commanding.ICommandContext;
import com.qianzhui.enode.commanding.ICommandHandler;
import com.qianzhui.enode.commanding.ICommandHandlerProvider;
import com.qianzhui.enode.commanding.ICommandHandlerProxy;
import com.qianzhui.enode.common.container.IObjectContainer;
import com.qianzhui.enode.infrastructure.impl.AbstractHandlerProvider;

import javax.inject.Inject;
import java.lang.reflect.Method;

public class DefaultCommandHandlerProvider extends AbstractHandlerProvider<Class, ICommandHandlerProxy, Class> implements ICommandHandlerProvider {
    private IObjectContainer objectContainer;

    @Inject
    public DefaultCommandHandlerProvider(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
    }

    public DefaultCommandHandlerProvider() {
    }

    @Override
    protected Class getGenericHandlerType() {
        return ICommandHandler.class;
    }

    @Override
    protected Class getKey(Method method) {
        return method.getParameterTypes()[1];
    }

    @Override
    protected Class getHandlerProxyImplementationType() {
        return CommandHandlerProxy.class;
    }

    @Override
    protected boolean isHandlerSourceMatchKey(Class handlerSource, Class key) {
        return key.isAssignableFrom(handlerSource);
    }

    protected boolean isHandleMethodMatchKey(Class[] argumentTypes, Class key) {
        return argumentTypes.length == 1 && argumentTypes[0] == key;
    }

    @Override
    protected boolean isHandleMethodMatch(Method method) {
        if (!"handleAsync".equals(method.getName())) {
            return false;
        }
        if (method.getParameterTypes().length != 2) {
            return false;
        }
        if (!ICommandContext.class.equals(method.getParameterTypes()[0])) {
            return false;
        }
        if (ICommand.class.equals(method.getParameterTypes()[1])) {
            return false;
        }
        if (!ICommand.class.isAssignableFrom(method.getParameterTypes()[1])) {
            return false;
        }
        return true;
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }
}
