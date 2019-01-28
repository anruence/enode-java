package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.ConfirmDepositCommand;
import com.enode.samples.domain.bank.deposittransaction.DepositTransaction;

import java.util.concurrent.CompletableFuture;

/**
 * 确认存款
 */
public class ConfirmDepositCommandHandler implements ICommandHandler<ConfirmDepositCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmDepositCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        future.thenAccept(DepositTransaction::ConfirmDeposit);
        return future;
    }
}