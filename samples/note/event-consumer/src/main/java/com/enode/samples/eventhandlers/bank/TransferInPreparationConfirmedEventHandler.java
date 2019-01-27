package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.bank.domain.transfertransaction.TransferInPreparationConfirmedEvent;

import java.util.concurrent.CompletableFuture;

public class TransferInPreparationConfirmedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferInPreparationConfirmedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferInPreparationConfirmedEvent message) {
        return handleAsyncInternal(message);
    }
}
