package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.bankaccount.InsufficientBalanceException;

import java.util.concurrent.CompletableFuture;

public class InsufficientBalanceExceptionHandler extends AbstractEventHandler implements IMessageHandler<InsufficientBalanceException> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(InsufficientBalanceException message) {
        return handleAsyncInternal(message);
    }
}
