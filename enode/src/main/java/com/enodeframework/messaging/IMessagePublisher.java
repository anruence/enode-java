package com.enodeframework.messaging;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.messaging.IMessage;

import java.util.concurrent.CompletableFuture;

public interface IMessagePublisher<TMessage extends IMessage> {
    CompletableFuture<AsyncTaskResult> publishAsync(TMessage message);
}
