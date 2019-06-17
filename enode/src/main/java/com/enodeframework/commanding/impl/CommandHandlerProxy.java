package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.commanding.ICommandHandlerProxy;
import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.infrastructure.WrappedRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class CommandHandlerProxy implements ICommandHandlerProxy {

    @Autowired
    private IObjectContainer objectContainer;

    private Class handlerType;

    private Object commandHandler;

    private MethodHandle methodHandle;

    private Method method;

    @Override
    public CompletableFuture<Void> handleAsync(ICommandContext context, ICommand command) {
        return CompletableFuture.runAsync(() -> {
            try {
                methodHandle.invoke(getInnerObject(), context, command);
            } catch (Throwable throwable) {
                throw new WrappedRuntimeException(throwable);
            }
        });
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
