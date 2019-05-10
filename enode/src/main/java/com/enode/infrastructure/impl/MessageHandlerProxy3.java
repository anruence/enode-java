package com.enode.infrastructure.impl;

import com.enode.common.container.IObjectContainer;
import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageHandlerProxy3;
import com.enode.infrastructure.IThreeMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageHandlerProxy3 implements IMessageHandlerProxy3 {

    @Autowired
    private IObjectContainer _objectContainer;

    private Class _handlerType;

    private Object _handler;

    private MethodHandle _methodHandle;

    private Method _method;

    private Class<?>[] _methodParameterTypes;

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2, IMessage message3) {
        IThreeMessageHandler handler = (IThreeMessageHandler) getInnerObject();
        List<Class<?>> parameterTypes = Arrays.asList(_methodParameterTypes);
        List<IMessage> params = new ArrayList<>();
        params.add(message1);
        params.add(message2);
        params.add(message3);

        //排序参数
        params.sort(Comparator.comparingInt(m -> getMessageParameterIndex(parameterTypes, m))
        );

        try {
            //参数按照方法定义参数类型列表传递
            return (CompletableFuture<AsyncTaskResult>) _methodHandle.invoke(handler, params.get(0), params.get(1), params.get(2));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private int getMessageParameterIndex(List<Class<?>> methodParameterTypes, IMessage message) {
        int i = 0;
        for (Class paramType : methodParameterTypes) {
            if (paramType.isAssignableFrom(message.getClass())) {
                return i;
            }
            i++;
        }

        return i;
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
