package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.applicationmessages.AccountValidateFailedMessage;

import java.util.concurrent.CompletableFuture;

public class AccountValidateFailedMessageHandler extends AbstractEventHandler implements IMessageHandler<AccountValidateFailedMessage> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(AccountValidateFailedMessage message) {
        return handleAsyncInternal(message);
    }
}
