package com.qianzhui.enode.infrastructure;

import com.qianzhui.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandler<T extends IMessage> {

    CompletableFuture<AsyncTaskResult> handleAsync(T message);
}