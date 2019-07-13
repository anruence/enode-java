package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.IORuntimeException;
import com.enodeframework.infrastructure.IMessage;
import com.enodeframework.infrastructure.IMessageHandlerProxy1;
import com.enodeframework.infrastructure.WrappedRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class MessageHandlerProxy1 implements IMessageHandlerProxy1 {

    @Autowired
    private IObjectContainer objectContainer;

    private Class handlerType;

    private Object handler;

    private MethodHandle methodHandle;

    private Method method;

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(IMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return (AsyncTaskResult) methodHandle.invoke(getInnerObject(), message);
            } catch (Throwable throwable) {
                if (throwable instanceof IORuntimeException) {
                    throw new IORuntimeException(throwable);
                }
                throw new WrappedRuntimeException(throwable);
            }
        });
    }

    @Override
    public Object getInnerObject() {
        if (handler != null) {
            return handler;
        }
        handler = objectContainer.resolve(handlerType);
        return handler;
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
