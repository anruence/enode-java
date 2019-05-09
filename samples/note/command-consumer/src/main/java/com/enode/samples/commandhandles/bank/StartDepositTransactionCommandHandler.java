package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.StartDepositTransactionCommand;
import com.enode.samples.domain.bank.deposittransaction.DepositTransaction;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 开始交易
 */
@Component
public class StartDepositTransactionCommandHandler implements ICommandHandler<StartDepositTransactionCommand> {

    @Override
    public CompletableFuture handleAsync(ICommandContext context, StartDepositTransactionCommand command) {
        return context.addAsync(new DepositTransaction(command.getAggregateRootId(), command.AccountId, command.Amount));
    }
}