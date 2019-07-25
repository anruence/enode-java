package com.enodeframework.commanding;

import com.enodeframework.infrastructure.IObjectProxy;
import com.enodeframework.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface ICommandHandlerProxy extends IObjectProxy, MethodInvocation {
    /**
     * Handle the given application command async. deal with aggregate in memory
     *
     * @param context
     * @param command
     * @return
     */
    CompletableFuture<Void> handleAsync(ICommandContext context, ICommand command);
}
