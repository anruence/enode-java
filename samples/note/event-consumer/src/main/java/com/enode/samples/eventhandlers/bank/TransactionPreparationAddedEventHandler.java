package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.bank.domain.bankaccount.AccountCreatedEvent;
import com.enode.samples.bank.domain.bankaccount.TransactionPreparationAddedEvent;

import java.util.concurrent.CompletableFuture;

public class TransactionPreparationAddedEventHandler extends AbstractEventHandler implements IMessageHandler<TransactionPreparationAddedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransactionPreparationAddedEvent message) {
        return handleAsyncInternal(message);
    }
}
