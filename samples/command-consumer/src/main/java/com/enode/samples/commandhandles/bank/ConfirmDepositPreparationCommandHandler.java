package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.ConfirmDepositPreparationCommand;
import com.enode.samples.domain.bank.deposittransaction.DepositTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 确认预存款
 */
@Component
public class ConfirmDepositPreparationCommandHandler implements ICommandHandler<ConfirmDepositPreparationCommand> {
    @Override
    public CompletableFuture handleAsync(ICommandContext context, ConfirmDepositPreparationCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        future.thenAccept(DepositTransaction::ConfirmDepositPreparation);
        return future;
    }
}