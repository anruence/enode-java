package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.ConfirmTransferInPreparationCommand;
import com.enode.samples.domain.bank.transfertransaction.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 确认预转入
 */
@Component
public class ConfirmTransferInPreparationCommandHandle implements ICommandHandler<ConfirmTransferInPreparationCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmTransferInPreparationCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.ConfirmTransferInPreparation());
        return future;
    }
}