package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;

import java.util.concurrent.CompletableFuture;

public class TransactionPreparationCommittedEventHandler extends AbstractEventHandler implements IMessageHandler<TransactionPreparationCommittedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransactionPreparationCommittedEvent message) {
        return handleAsyncInternal(message);
    }
}
