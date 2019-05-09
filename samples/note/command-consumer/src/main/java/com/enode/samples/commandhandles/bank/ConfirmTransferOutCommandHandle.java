package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.ConfirmTransferOutCommand;
import com.enode.samples.domain.bank.transfertransaction.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ConfirmTransferOutCommandHandle implements ICommandHandler<ConfirmTransferOutCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmTransferOutCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.ConfirmTransferOut());
        return future;
    }
}