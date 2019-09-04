package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.common.exception.ENodeRuntimeException;
import com.enodeframework.common.exception.IORuntimeException;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IMessage;
import com.enodeframework.infrastructure.IMessageHandlerProxy2;
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
        Object result;
        CompletableFuture<AsyncTaskResult> future = new CompletableFuture<>();
        try {
            if (methodParameterTypes[0].isAssignableFrom(message1.getClass())) {
                result = methodHandle.invoke(getInnerObject(), message1, message2);
            } else {
                result = methodHandle.invoke(getInnerObject(), message2, message1);
            }
            if (result instanceof CompletableFuture) {
                return (CompletableFuture<AsyncTaskResult>) result;
            }
            future.complete((AsyncTaskResult) result);
        } catch (Throwable throwable) {
            if (throwable.getCause() instanceof IORuntimeException) {
                throwable = new IORuntimeException(throwable);
            }
            future.completeExceptionally(throwable);
        }
        return future;
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
