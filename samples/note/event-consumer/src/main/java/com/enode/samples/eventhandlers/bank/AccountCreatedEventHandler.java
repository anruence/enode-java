package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.bankaccount.AccountCreatedEvent;

import java.util.concurrent.CompletableFuture;

public class AccountCreatedEventHandler extends AbstractEventHandler implements IMessageHandler<AccountCreatedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(AccountCreatedEvent message) {
        return handleAsyncInternal(message);
    }
}
