package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.common.container.IObjectContainer;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IMessage;
import com.qianzhui.enode.infrastructure.IMessageHandler;
import com.qianzhui.enode.infrastructure.IMessageHandlerProxy2;
import com.qianzhui.enode.infrastructure.ITwoMessageHandler;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class MessageHandlerProxy2 implements IMessageHandlerProxy2 {
    private IObjectContainer _objectContainer;
    private Class _handlerType;
    private ITwoMessageHandler _handler;
    private MethodHandle _methodHandle;
    private Method _method;
    private Class<?>[] _methodParameterTypes;

    public MessageHandlerProxy2(IObjectContainer objectContainer, Class handlerType, ITwoMessageHandler handler, MethodHandle methodHandle, Method method) {
        _objectContainer = objectContainer;
        _handlerType = handlerType;
        _handler = handler;
        _methodHandle = methodHandle;
        _method = method;
        _methodParameterTypes = method.getParameterTypes();
    }

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2) {
        IMessageHandler handler = (IMessageHandler) getInnerObject();

        try {
            if (_methodParameterTypes[0].isAssignableFrom(message1.getClass())) {
                return (CompletableFuture<AsyncTaskResult>) _methodHandle.invoke(handler, message1, message2);
            } else {
                return (CompletableFuture<AsyncTaskResult>) _methodHandle.invoke(handler, message2, message1);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public Object getInnerObject() {
        if (_handler != null) {
            return _handler;
        }

        return _objectContainer.resolve(_handlerType);
    }

    @Override
    public Method getMethod() {
        return _method;
    }
}
