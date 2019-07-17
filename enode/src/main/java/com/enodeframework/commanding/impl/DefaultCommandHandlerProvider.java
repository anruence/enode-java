package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandlerProvider;
import com.enodeframework.commanding.ICommandHandlerProxy;
import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.infrastructure.impl.AbstractHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * @author anruence@gmail.com
 */
public class DefaultCommandHandlerProvider extends AbstractHandlerProvider<Class, ICommandHandlerProxy, Class> implements ICommandHandlerProvider {

    @Autowired
    private IObjectContainer objectContainer;

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
        return key.equals(handlerSource);
    }

    @Override
    protected boolean isHandleMethodMatch(Method method) {
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
        if (!isMethodAnnotationSubscribe(method)) {
            return false;
        }
        return true;
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return objectContainer;
    }

    public DefaultCommandHandlerProvider setObjectContainer(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
        return this;
    }
}
