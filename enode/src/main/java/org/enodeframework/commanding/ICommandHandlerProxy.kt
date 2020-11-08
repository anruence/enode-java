package org.enodeframework.commanding

import org.enodeframework.infrastructure.IObjectProxy
import org.enodeframework.infrastructure.MethodInvocation
import java.util.concurrent.CompletableFuture

interface ICommandHandlerProxy : IObjectProxy, MethodInvocation {
    /**
     * Handle the given application command async. deal with aggregate in memory
     */
    fun handleAsync(context: ICommandContext, command: ICommand): CompletableFuture<Boolean>
}