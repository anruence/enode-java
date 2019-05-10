package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.applicationmessages.AccountValidatePassedMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class AccountValidatePassedMessageHandler extends AbstractEventHandler implements IMessageHandler<AccountValidatePassedMessage> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(AccountValidatePassedMessage message) {
        return handleAsyncInternal(message);
    }
}
