package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.CancelTransferTransactionCommand;
import com.enode.samples.domain.bank.transfertransaction.TransferTransaction;

import java.util.concurrent.CompletableFuture;

public class CancelTransferTransactionCommandHandle implements ICommandHandler<CancelTransferTransactionCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, CancelTransferTransactionCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.Cancel());
        return future;
    }
}