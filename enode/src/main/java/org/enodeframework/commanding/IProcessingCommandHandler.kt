package org.enodeframework.commanding

import java.util.concurrent.CompletableFuture

interface IProcessingCommandHandler {
    fun handleAsync(processingCommand: ProcessingCommand): CompletableFuture<Void>
}