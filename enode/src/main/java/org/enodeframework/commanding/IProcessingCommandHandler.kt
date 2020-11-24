package org.enodeframework.commanding

import java.util.concurrent.CompletableFuture

interface IProcessingCommandHandler {
    /**
     * process given processing command.
     */
    fun handleAsync(processingCommand: ProcessingCommand): CompletableFuture<Boolean>
}