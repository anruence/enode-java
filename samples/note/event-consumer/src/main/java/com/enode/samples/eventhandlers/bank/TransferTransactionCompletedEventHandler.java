package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.transfertransaction.TransferTransactionCompletedEvent;

import java.util.concurrent.CompletableFuture;

public class TransferTransactionCompletedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferTransactionCompletedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferTransactionCompletedEvent message) {
        return handleAsyncInternal(message);
    }
}
