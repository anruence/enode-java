package com.enode.infrastructure;

import com.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandler<T extends IMessage> {

    CompletableFuture<AsyncTaskResult> handleAsync(T message);
}