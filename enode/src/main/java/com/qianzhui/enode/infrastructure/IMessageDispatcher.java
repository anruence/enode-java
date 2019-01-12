package com.qianzhui.enode.infrastructure;

import com.qianzhui.enode.common.io.AsyncTaskResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IMessageDispatcher {
    CompletableFuture<AsyncTaskResult> dispatchMessageAsync(IMessage message);

    CompletableFuture<AsyncTaskResult> dispatchMessagesAsync(List<? extends IMessage> messages);
}
