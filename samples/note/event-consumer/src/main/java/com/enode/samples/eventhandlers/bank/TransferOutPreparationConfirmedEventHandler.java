package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.bank.domain.bankaccount.AccountCreatedEvent;
import com.enode.samples.bank.domain.transfertransaction.TransferOutPreparationConfirmedEvent;

import java.util.concurrent.CompletableFuture;

public class TransferOutPreparationConfirmedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferOutPreparationConfirmedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferOutPreparationConfirmedEvent message) {
        return handleAsyncInternal(message);
    }
}
