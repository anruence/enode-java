package com.enode.infrastructure.impl;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class DoNothingPublisher<TMessage extends IMessage> implements IMessagePublisher<TMessage> {

    private static final CompletableFuture<AsyncTaskResult> SUCCESSRESULTTASK = CompletableFuture.completedFuture(AsyncTaskResult.Success);

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(TMessage message) {
        return SUCCESSRESULTTASK;
    }
}
