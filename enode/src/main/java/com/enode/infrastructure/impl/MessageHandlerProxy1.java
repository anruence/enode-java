package com.enode.infrastructure.impl;

import com.enode.common.container.IObjectContainer;
import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageHandler;
import com.enode.infrastructure.IMessageHandlerProxy1;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class MessageHandlerProxy1 implements IMessageHandlerProxy1 {

    @Autowired
    private IObjectContainer _objectContainer;

    private Class _handlerType;

    private Object _handler;

    private MethodHandle _methodHandle;

    private Method _method;

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(IMessage message) {
        IMessageHandler handler = (IMessageHandler) getInnerObject();
        try {
            return (CompletableFuture<AsyncTaskResult>) _methodHandle.invoke(handler, message);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public Object getInnerObject() {
        if (_handler != null) {
            return _handler;
        }
        _handler = _objectContainer.resolve(_handlerType);
        return _handler;
    }

    @Override
    public void setHandlerType(Class handlerType) {
        this._handlerType = handlerType;
    }

    @Override
    public void setMethodHandle(MethodHandle methodHandle) {
        this._methodHandle = methodHandle;
    }

    @Override
    public Method getMethod() {
        return _method;
    }

    @Override
    public void setMethod(Method method) {
        this._method = method;
    }

}
