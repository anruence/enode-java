package org.enodeframework.messaging;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IMessageDispatcher {
    CompletableFuture<Void> dispatchMessageAsync(IMessage message);

    CompletableFuture<Void> dispatchMessagesAsync(List<? extends IMessage> messages);
}
