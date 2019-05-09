package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.transfertransaction.TransferTransactionCanceledEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransferTransactionCanceledEventHandler extends AbstractEventHandler implements IMessageHandler<TransferTransactionCanceledEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferTransactionCanceledEvent message) {
        return handleAsyncInternal(message);
    }
}
