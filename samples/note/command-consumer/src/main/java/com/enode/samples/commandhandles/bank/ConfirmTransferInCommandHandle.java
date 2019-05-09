package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.ConfirmTransferInCommand;
import com.enode.samples.domain.bank.transfertransaction.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ConfirmTransferInCommandHandle implements ICommandHandler<ConfirmTransferInCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmTransferInCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.ConfirmTransferIn());
        return future;
    }
}