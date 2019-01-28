package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.transfertransaction.TransferTransactionCanceledEvent;

import java.util.concurrent.CompletableFuture;

public class TransferTransactionCanceledEventHandler extends AbstractEventHandler implements IMessageHandler<TransferTransactionCanceledEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferTransactionCanceledEvent message) {
        return handleAsyncInternal(message);
    }
}
