package org.enodeframework.messaging.impl;

import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.messaging.IMessage;
import org.enodeframework.messaging.IMessageHandlerProxy2;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class MessageHandlerProxy2 implements IMessageHandlerProxy2 {
    private Class<?> handlerType;
    private Object handler;
    private MethodHandle methodHandle;
    private Method method;
    private Class<?>[] methodParameterTypes;

    @Override
    public CompletableFuture<Void> handleAsync(IMessage message1, IMessage message2) {
        Object result;
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            if (methodParameterTypes[0].isAssignableFrom(message1.getClass())) {
                result = methodHandle.invoke(getInnerObject(), message1, message2);
            } else {
                result = methodHandle.invoke(getInnerObject(), message2, message1);
            }
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
        if (handler != null) {
            return handler;
        }
        handler = ObjectContainer.INSTANCE.resolve(handlerType);
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
