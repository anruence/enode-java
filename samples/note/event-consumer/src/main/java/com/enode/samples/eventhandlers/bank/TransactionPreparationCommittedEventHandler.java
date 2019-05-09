package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransactionPreparationCommittedEventHandler extends AbstractEventHandler implements IMessageHandler<TransactionPreparationCommittedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransactionPreparationCommittedEvent message) {
        return handleAsyncInternal(message);
    }
}
