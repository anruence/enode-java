package com.enode.infrastructure.impl;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageDispatcher;
import com.enode.infrastructure.IProcessingMessage;
import com.enode.infrastructure.IProcessingMessageHandler;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class DefaultProcessingMessageHandler<X extends IProcessingMessage<X, Y>, Y extends IMessage> implements IProcessingMessageHandler<X, Y> {
    private final IMessageDispatcher _dispatcher;

    @Inject
    public DefaultProcessingMessageHandler(IMessageDispatcher dispatcher) {
        _dispatcher = dispatcher;
    }

    @Override
    public void handleAsync(X processingMessage) {
        CompletableFuture<AsyncTaskResult> asyncTaskResultCompletableFuture = _dispatcher.dispatchMessageAsync(processingMessage.getMessage());
        asyncTaskResultCompletableFuture.thenRun(() ->
                processingMessage.complete()
        );
    }
}
