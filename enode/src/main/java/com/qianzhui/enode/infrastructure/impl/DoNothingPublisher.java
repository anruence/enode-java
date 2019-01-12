package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.infrastructure.IMessage;
import com.qianzhui.enode.infrastructure.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class DoNothingPublisher<TMessage extends IMessage> implements IMessagePublisher<TMessage> {
    private static final CompletableFuture<AsyncTaskResult> _successResultTask = CompletableFuture.completedFuture(AsyncTaskResult.Success);

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(TMessage message) {
        return _successResultTask;
    }
}
