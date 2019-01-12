package com.qianzhui.enode.infrastructure;

import com.qianzhui.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IMessagePublisher<TMessage extends IMessage> {
    CompletableFuture<AsyncTaskResult> publishAsync(TMessage message);
}
