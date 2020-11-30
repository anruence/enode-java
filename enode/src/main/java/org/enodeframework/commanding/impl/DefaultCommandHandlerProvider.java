package org.enodeframework.commanding.impl;

import kotlin.coroutines.Continuation;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.commanding.ICommandHandlerProvider;
import org.enodeframework.commanding.ICommandHandlerProxy;
import org.enodeframework.common.container.IObjectContainer;
import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.infrastructure.impl.AbstractHandlerProvider;

import java.lang.reflect.Method;

/**
 * @author anruence@gmail.com
 */
public class DefaultCommandHandlerProvider extends AbstractHandlerProvider<Class<?>, ICommandHandlerProxy, Class<?>> implements ICommandHandlerProvider {

    @Override
    protected Class<?> getKey(Method method) {
        return method.getParameterTypes()[1];
    }

    @Override
    protected Class<? extends CommandHandlerProxy> getHandlerProxyImplementationType() {
        return CommandHandlerProxy.class;
    }

    @Override
    protected boolean isHandlerSourceMatchKey(Class<?> handlerSource, Class<?> key) {
        return key.equals(handlerSource);
    }

    @Override
    protected boolean isHandleMethodMatch(Method method) {
        int paramCount = method.getParameterTypes().length;
        if (paramCount != 2) {
            if (!(isSuspendMethod(method))) {
                return false;
            }
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
        return isMethodAnnotationSubscribe(method);
    }

    @Override
    protected boolean isSuspendMethod(Method method) {
        return method.getParameterTypes().length == 3 && Continuation.class.equals(method.getParameterTypes()[2]);
    }

    @Override
    protected IObjectContainer getObjectContainer() {
        return ObjectContainer.INSTANCE;
    }
}
