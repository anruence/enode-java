package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.bank.commands.ConfirmTransferInPreparationCommand;
import com.enode.samples.bank.domain.transfertransaction.TransferTransaction;

import java.util.concurrent.CompletableFuture;

/**
 * 确认预转入
 */
public class ConfirmTransferOutPreparationCommandHandle implements ICommandHandler<ConfirmTransferInPreparationCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmTransferInPreparationCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.ConfirmTransferOutPreparation());
        return future;
    }
}