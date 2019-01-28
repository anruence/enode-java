package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.transfertransaction.TransferTransactionStartedEvent;

import java.util.concurrent.CompletableFuture;

public class TransferTransactionStartedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferTransactionStartedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferTransactionStartedEvent message) {
        return handleAsyncInternal(message);
    }
}
