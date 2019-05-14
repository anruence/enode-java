package com.enode.infrastructure.impl;

import com.enode.common.container.IObjectContainer;
import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageHandlerProxy2;
import com.enode.infrastructure.ITwoMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

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
        ITwoMessageHandler handler = (ITwoMessageHandler) getInnerObject();
        try {
            if (methodParameterTypes[0].isAssignableFrom(message1.getClass())) {
                return (CompletableFuture<AsyncTaskResult>) methodHandle.invoke(handler, message1, message2);
            } else {
                return (CompletableFuture<AsyncTaskResult>) methodHandle.invoke(handler, message2, message1);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
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
