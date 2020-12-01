package org.enodeframework.messaging

interface ITwoMessageHandlerProvider {
    fun getHandlers(messageTypes: List<Class<*>>): List<MessageHandlerData<IMessageHandlerProxy2>>
}