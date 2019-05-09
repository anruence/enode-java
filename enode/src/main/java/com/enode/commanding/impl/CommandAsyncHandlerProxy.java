package com.enode.commanding.impl;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandAsyncHandler;
import com.enode.commanding.ICommandAsyncHandlerProxy;
import com.enode.common.container.IObjectContainer;
import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IApplicationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CommandAsyncHandlerProxy implements ICommandAsyncHandlerProxy {

    @Autowired
    private IObjectContainer _objectContainer;

    private Class _handlerType;

    private Object _commandHandler;

    private MethodHandle _methodHandle;

    private Method _method;

    @Override
    public CompletableFuture<AsyncTaskResult<IApplicationMessage>> handleAsync(ICommand command) {
        ICommandAsyncHandler handler = (ICommandAsyncHandler) getInnerObject();
        try {
            return (CompletableFuture<AsyncTaskResult<IApplicationMessage>>) _methodHandle.invoke(handler, command);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getInnerObject() {
        if (_commandHandler != null) {
            return _commandHandler;
        }
        _commandHandler = _objectContainer.resolve(_handlerType);
        return _commandHandler;
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
