package org.enodeframework.messaging

import java.util.concurrent.CompletableFuture

interface IMessageDispatcher {
    fun dispatchMessageAsync(message: IMessage): CompletableFuture<Boolean>
    fun dispatchMessagesAsync(messages: List<IMessage>): CompletableFuture<Boolean>
}