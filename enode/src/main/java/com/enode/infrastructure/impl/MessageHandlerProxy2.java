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
    private IObjectContainer _objectContainer;

    private Class _handlerType;

    private Object _handler;

    private MethodHandle _methodHandle;

    private Method _method;

    private Class<?>[] _methodParameterTypes;

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2) {
        ITwoMessageHandler handler = (ITwoMessageHandler) getInnerObject();
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
        _methodParameterTypes = method.getParameterTypes();
    }

}
