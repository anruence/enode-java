package org.enodeframework.messaging

interface IThreeMessageHandlerProvider {
    fun getHandlers(messageTypes: List<Class<*>>): List<MessageHandlerData<IMessageHandlerProxy3>>
}