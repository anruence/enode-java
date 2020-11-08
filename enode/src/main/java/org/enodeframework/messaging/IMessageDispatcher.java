package org.enodeframework.messaging;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IMessageDispatcher {
    CompletableFuture<Boolean> dispatchMessageAsync(IMessage message);

    CompletableFuture<Boolean> dispatchMessagesAsync(List<? extends IMessage> messages);
}
