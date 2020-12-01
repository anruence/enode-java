package org.enodeframework.messaging

interface IMessageHandlerProvider {
    fun getHandlers(messageType: Class<*>): List<MessageHandlerData<IMessageHandlerProxy1>>
}