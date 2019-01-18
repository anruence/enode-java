package com.enode.commanding;

import com.enode.infrastructure.IObjectProxy;
import com.enode.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface ICommandHandlerProxy extends IObjectProxy, MethodInvocation {
    CompletableFuture handleAsync(ICommandContext context, ICommand command);
}
