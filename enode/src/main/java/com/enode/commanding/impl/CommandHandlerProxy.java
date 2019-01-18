package com.enode.commanding.impl;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.commanding.ICommandHandlerProxy;
import com.enode.common.container.IObjectContainer;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class CommandHandlerProxy implements ICommandHandlerProxy {
    private IObjectContainer _objectContainer;
    private Class _commandHandlerType;
    private ICommandHandler _commandHandler;
    private MethodHandle _methodHandle;
    private Method _method;

    public CommandHandlerProxy(IObjectContainer objectContainer, Class commandHandlerType, ICommandHandler commandHandler, MethodHandle methodHandle, Method method) {
        _objectContainer = objectContainer;
        _commandHandlerType = commandHandlerType;
        _commandHandler = commandHandler;
        _methodHandle = methodHandle;
        _method = method;
    }

    @Override
    public CompletableFuture handleAsync(ICommandContext context, ICommand command) {
        ICommandHandler handler = (ICommandHandler) getInnerObject();
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            try {
                return _methodHandle.invoke(handler, context, command);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
        return future;
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
