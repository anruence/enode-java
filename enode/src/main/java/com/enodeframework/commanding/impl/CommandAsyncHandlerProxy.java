package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandAsyncHandlerProxy;
import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.common.exception.EnodeRuntimeException;
import com.enodeframework.common.exception.IORuntimeException;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IApplicationMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class CommandAsyncHandlerProxy implements ICommandAsyncHandlerProxy {

    @Autowired
    private IObjectContainer objectContainer;

    private Class handlerType;

    private Object commandHandler;

    private MethodHandle methodHandle;

    private Method method;

    @Override
    public CompletableFuture<AsyncTaskResult<IApplicationMessage>> handleAsync(ICommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return (AsyncTaskResult<IApplicationMessage>) methodHandle.invoke(getInnerObject(), command);
            } catch (Throwable throwable) {
                if (throwable instanceof IORuntimeException) {
                    throw new IORuntimeException(throwable);
                }
                throw new EnodeRuntimeException(throwable);
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
