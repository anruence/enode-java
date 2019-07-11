package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandAsyncHandlerProvider;
import com.enodeframework.commanding.ICommandAsyncHandlerProxy;
import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.infrastructure.impl.AbstractHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * @author anruence@gmail.com
 */
public class DefaultCommandAsyncHandlerProvider extends AbstractHandlerProvider<Class, ICommandAsyncHandlerProxy, Class> implements ICommandAsyncHandlerProvider {

    @Autowired
    private IObjectContainer objectContainer;

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
        if (method.getParameterTypes().length != 1) {
            return false;
        }
        if (ICommand.class.equals(method.getParameterTypes()[0])) {
            return false;
        }
        if (!ICommand.class.isAssignableFrom(method.getParameterTypes()[0])) {
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
}
