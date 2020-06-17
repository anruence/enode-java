package org.enodeframework.commanding.impl;

import org.enodeframework.ObjectContainer;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.commanding.ICommandHandlerProxy;
import org.enodeframework.common.container.IObjectContainer;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class CommandHandlerProxy implements ICommandHandlerProxy {

    private Class handlerType;
    private Object commandHandler;
    private MethodHandle methodHandle;
    private Method method;

    @Override
    public CompletableFuture<Void> handleAsync(ICommandContext context, ICommand command) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            Object result = methodHandle.invoke(getInnerObject(), context, command);
            if (result instanceof CompletableFuture) {
                return (CompletableFuture<Void>) result;
            }
            future.complete(null);
        } catch (Throwable throwable) {
            future.completeExceptionally(throwable);
        }
        return future;
    }

    @Override
    public Object getInnerObject() {
        if (commandHandler != null) {
            return commandHandler;
        }
        commandHandler = ObjectContainer.container.resolve(handlerType);
        return commandHandler;
    }

    @Override
    public void setHandlerType(Class handlerType) {
        this.handlerType = handlerType;
    }

    @Override
    public void setMethodHandle(MethodHandle methodHandle) {
        this.methodHandle = methodHandle;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void setMethod(Method method) {
        this.method = method;
    }
}
