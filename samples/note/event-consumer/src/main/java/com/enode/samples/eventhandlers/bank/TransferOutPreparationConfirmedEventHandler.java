package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import com.enode.samples.domain.bank.transfertransaction.TransferOutPreparationConfirmedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransferOutPreparationConfirmedEventHandler extends AbstractEventHandler implements IMessageHandler<TransferOutPreparationConfirmedEvent> {
    @Override
    public CompletableFuture<AsyncTaskResult> handleAsync(TransferOutPreparationConfirmedEvent message) {
        return handleAsyncInternal(message);
    }
}
