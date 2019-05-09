package com.enode.commanding.impl;

import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.commanding.ICommandHandlerProxy;
import com.enode.common.container.IObjectContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CommandHandlerProxy implements ICommandHandlerProxy {

    @Autowired
    private IObjectContainer _objectContainer;

    private Class _handlerType;

    private Object _commandHandler;

    private MethodHandle _methodHandle;

    private Method _method;

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
