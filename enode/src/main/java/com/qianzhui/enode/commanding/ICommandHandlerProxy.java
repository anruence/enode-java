package com.qianzhui.enode.commanding;

import com.qianzhui.enode.infrastructure.IObjectProxy;
import com.qianzhui.enode.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface ICommandHandlerProxy extends IObjectProxy, MethodInvocation {
    CompletableFuture handleAsync(ICommandContext context, ICommand command);
}
