package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransactionPreparationAddedEventHandler extends AbstractEventHandler implements IMessageHandler<TransactionPreparationAddedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransactionPreparationAddedEvent message) {
        return handleAsyncInternal(message);
    }
}
