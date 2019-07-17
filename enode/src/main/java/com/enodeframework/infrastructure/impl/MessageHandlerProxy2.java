package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.exception.IORuntimeException;
import com.enodeframework.infrastructure.IMessage;
import com.enodeframework.infrastructure.IMessageHandlerProxy2;
import com.enodeframework.common.exception.EnodeRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class MessageHandlerProxy2 implements IMessageHandlerProxy2 {

    @Autowired
    private IObjectContainer objectContainer;

    private Class handlerType;

    private Object handler;

    private MethodHandle methodHandle;

    private Method method;

    private Class<?>[] methodParameterTypes;

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (methodParameterTypes[0].isAssignableFrom(message1.getClass())) {
                    return (AsyncTaskResult) methodHandle.invoke(getInnerObject(), message1, message2);
                } else {
                    return (AsyncTaskResult) methodHandle.invoke(getInnerObject(), message2, message1);
                }
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
        methodParameterTypes = method.getParameterTypes();
    }

}
