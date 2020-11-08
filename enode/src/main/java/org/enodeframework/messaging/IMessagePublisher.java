package org.enodeframework.messaging;

import java.util.concurrent.CompletableFuture;

public interface IMessagePublisher<TMessage extends IMessage> {
    CompletableFuture<Boolean> publishAsync(TMessage message);
}
