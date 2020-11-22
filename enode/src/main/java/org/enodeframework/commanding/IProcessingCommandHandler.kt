package org.enodeframework.commanding

import kotlinx.coroutines.Deferred

interface IProcessingCommandHandler {
    /**
     * process given processing command.
     */
    suspend fun handleAsync(processingCommand: ProcessingCommand): Deferred<Boolean>
}