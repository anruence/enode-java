package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandlerProxy;
import com.enodeframework.common.container.IObjectContainer;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class CommandHandlerProxy implements ICommandHandlerProxy {

    @Autowired
    private IObjectContainer objectContainer;
    private Class handlerType;
    private Object commandHandler;
    private MethodHandle methodHandle;
    private Method method;

    public CommandHandlerProxy setObjectContainer(IObjectContainer objectContainer) {
        this.objectContainer = objectContainer;
        return this;
    }

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
        commandHandler = objectContainer.resolve(handlerType);
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
