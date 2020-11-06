package org.enodeframework.commanding

import org.enodeframework.messaging.MessageHandlerData

/**
 * Represents a provider to provide the command handlers.
 */
interface ICommandHandlerProvider {
    /**
     * Get all the handlers for the given command type.
     */
    fun getHandlers(commandType: Class<*>?): List<MessageHandlerData<ICommandHandlerProxy?>?>?
}