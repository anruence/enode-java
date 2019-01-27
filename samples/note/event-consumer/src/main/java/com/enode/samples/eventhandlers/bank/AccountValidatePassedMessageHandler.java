package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.applicationmessages.AccountValidatePassedMessage;

import java.util.concurrent.CompletableFuture;

public class AccountValidatePassedMessageHandler extends AbstractEventHandler implements IMessageHandler<AccountValidatePassedMessage> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(AccountValidatePassedMessage message) {
        return handleAsyncInternal(message);
    }
}
