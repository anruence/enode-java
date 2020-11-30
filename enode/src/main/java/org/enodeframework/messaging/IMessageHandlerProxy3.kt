package org.enodeframework.messaging

import org.enodeframework.infrastructure.IObjectProxy
import org.enodeframework.infrastructure.MethodInvocation

interface IMessageHandlerProxy3 : IObjectProxy, MethodInvocation {
    suspend fun handleAsync(message1: IMessage, message2: IMessage, message3: IMessage)
}