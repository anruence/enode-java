package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.bank.domain.transfertransaction.TransferTransactionCompletedEvent;

import java.util.concurrent.CompletableFuture;

public class AysncCountHandler extends AbstractEventHandler implements IMessageHandler<TransferTransactionCompletedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferTransactionCompletedEvent message) {
        return handleAsyncInternal(message);
    }
}