package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.container.IObjectContainer;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.IORuntimeException;
import com.enodeframework.infrastructure.IMessage;
import com.enodeframework.infrastructure.IMessageHandlerProxy3;
import com.enodeframework.infrastructure.WrappedRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class MessageHandlerProxy3 implements IMessageHandlerProxy3 {

    @Autowired
    private IObjectContainer objectContainer;

    private Class handlerType;

    private Object handler;

    private MethodHandle methodHandle;

    private Method method;

    private Class<?>[] methodParameterTypes;

    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(IMessage message1, IMessage message2, IMessage message3) {
        return CompletableFuture.supplyAsync(() -> {
            List<Class<?>> parameterTypes = Arrays.asList(methodParameterTypes);
            List<IMessage> params = new ArrayList<>();
            params.add(message1);
            params.add(message2);
            params.add(message3);
            //排序参数
            params.sort(Comparator.comparingInt(m -> getMessageParameterIndex(parameterTypes, m))
            );
            try {
                //参数按照方法定义参数类型列表传递
                return (AsyncTaskResult) methodHandle.invoke(getInnerObject(), params.get(0), params.get(1), params.get(2));
            } catch (Throwable throwable) {
                if (throwable instanceof IORuntimeException) {
                    throw new IORuntimeException(throwable);
                }
                throw new WrappedRuntimeException(throwable);
            }
        });
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
