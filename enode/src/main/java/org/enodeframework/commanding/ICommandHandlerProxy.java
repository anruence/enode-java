package org.enodeframework.commanding;

import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.MethodInvocation;

import java.util.concurrent.CompletableFuture;

public interface ICommandHandlerProxy extends IObjectProxy, MethodInvocation {
    /**
     * Handle the given application command async. deal with aggregate in memory
     */
    CompletableFuture<Void> handleAsync(ICommandContext context, ICommand command);
}
