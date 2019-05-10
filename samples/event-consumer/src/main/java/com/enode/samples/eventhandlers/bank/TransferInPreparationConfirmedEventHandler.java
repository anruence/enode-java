package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.transfertransaction.TransferInPreparationConfirmedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransferInPreparationConfirmedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferInPreparationConfirmedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferInPreparationConfirmedEvent message) {
        return handleAsyncInternal(message);
    }
}
