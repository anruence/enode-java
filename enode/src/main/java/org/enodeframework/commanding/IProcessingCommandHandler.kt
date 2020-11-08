package org.enodeframework.commanding

import java.util.concurrent.CompletableFuture

interface IProcessingCommandHandler {
   suspend fun handleAsync(processingCommand: ProcessingCommand): CompletableFuture<Boolean>
}