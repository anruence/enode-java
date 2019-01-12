package com.qianzhui.enode.commanding.impl;

import com.qianzhui.enode.commanding.ICommand;
import com.qianzhui.enode.commanding.ICommandAsyncHandler;
import com.qianzhui.enode.commanding.ICommandAsyncHandlerProxy;
import com.qianzhui.enode.common.container.IObjectContainer;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IApplicationMessage;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class CommandAsyncHandlerProxy implements ICommandAsyncHandlerProxy {
    private IObjectContainer _objectContainer;
    private Class _commandHandlerType;
    private ICommandAsyncHandler _commandHandler;
    private MethodHandle _methodHandle;
    private Method _method;

    public CommandAsyncHandlerProxy(IObjectContainer objectContainer, Class commandHandlerType, ICommandAsyncHandler commandHandler, MethodHandle methodHandle, Method method) {
        _objectContainer = objectContainer;
        _commandHandlerType = commandHandlerType;
        _commandHandler = commandHandler;
        _methodHandle = methodHandle;
        _method = method;
    }

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

        return _objectContainer.resolve(_commandHandlerType);
    }

    @Override
    public Method getMethod() {
        return _method;
    }
}
