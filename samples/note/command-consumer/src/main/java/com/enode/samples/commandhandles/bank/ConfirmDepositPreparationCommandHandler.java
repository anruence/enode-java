package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.bank.commands.ConfirmDepositPreparationCommand;
import com.enode.samples.bank.domain.deposittransaction.DepositTransaction;

import java.util.concurrent.CompletableFuture;

/**
 * 确认预存款
 */
public class ConfirmDepositPreparationCommandHandler implements ICommandHandler<ConfirmDepositPreparationCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmDepositPreparationCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        future.thenAccept(DepositTransaction::ConfirmDepositPreparation);
        return future;
    }
}