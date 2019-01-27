package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.bank.commands.ConfirmAccountValidatePassedCommand;
import com.enode.samples.bank.domain.transfertransaction.TransferTransaction;

import java.util.concurrent.CompletableFuture;

public class ConfirmAccountValidatePassedCommandHandler implements ICommandHandler<ConfirmAccountValidatePassedCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmAccountValidatePassedCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        future.thenAccept(transaction -> transaction.ConfirmAccountValidatePassed(command.AccountId));
        return future;
    }
}