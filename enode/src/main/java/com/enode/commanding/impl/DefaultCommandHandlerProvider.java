package com.enode.commanding.impl;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.commanding.ICommandHandlerProvider;
import com.enode.commanding.ICommandHandlerProxy;
import com.enode.common.Constants;
import com.enode.common.container.IObjectContainer;
import com.enode.infrastructure.impl.AbstractHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

public class DefaultCommandHandlerProvider extends AbstractHandlerProvider<Class, ICommandHandlerProxy, Class> implements ICommandHandlerProvider {

    @Autowired
    private IObjectContainer objectContainer;

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
        if (!Constants.HANDLE_METHOD.equals(method.getName())) {
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
