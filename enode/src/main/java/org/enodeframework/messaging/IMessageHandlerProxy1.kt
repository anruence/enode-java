package org.enodeframework.messaging

import org.enodeframework.infrastructure.IObjectProxy
import org.enodeframework.infrastructure.MethodInvocation

interface IMessageHandlerProxy1 : IObjectProxy, MethodInvocation {
    suspend fun handleAsync(message: IMessage)
}