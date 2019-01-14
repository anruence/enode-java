package com.qianzhui.enode.infrastructure;

import com.qianzhui.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IThreeMessageHandler<T extends IMessage, E extends IMessage, D extends IMessage> {

    CompletableFuture<AsyncTaskResult> handleAsync(T message1, E message2, D message3);
}