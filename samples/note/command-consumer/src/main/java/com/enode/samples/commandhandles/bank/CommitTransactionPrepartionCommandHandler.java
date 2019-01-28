package com.enode.samples.commandhandles.bank;

import com.enode.commanding.ICommandContext;
import com.enode.commanding.ICommandHandler;
import com.enode.samples.commands.bank.AddTransactionPreparationCommand;
import com.enode.samples.domain.bank.bankaccount.BankAccount;

import java.util.concurrent.CompletableFuture;

/// <summary>银行账户相关命令处理
/// </summary>
public class CommitTransactionPrepartionCommandHandler implements ICommandHandler<AddTransactionPreparationCommand>                   //开户
{
    @Override
    public CompletableFuture handleAsync(ICommandContext context, AddTransactionPreparationCommand command) {
        CompletableFuture<BankAccount> future = context.getAsync(command.getAggregateRootId(), BankAccount.class);
        future.thenAccept(account -> account.CommitTransactionPreparation(command.TransactionId));
        return future;
    }
}
