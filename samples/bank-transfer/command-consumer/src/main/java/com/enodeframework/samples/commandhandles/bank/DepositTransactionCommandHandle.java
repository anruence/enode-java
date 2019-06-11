package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.annotation.Command;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.common.io.Task;
import com.enodeframework.samples.commands.bank.ConfirmDepositCommand;
import com.enodeframework.samples.commands.bank.ConfirmDepositPreparationCommand;
import com.enodeframework.samples.commands.bank.StartDepositTransactionCommand;
import com.enodeframework.samples.domain.bank.deposittransaction.DepositTransaction;

import java.util.concurrent.CompletableFuture;

/**
 * 银行存款交易相关命令处理
 * <StartDepositTransactionCommand>,                      //开始交易
 * <ConfirmDepositPreparationCommand>,                    //确认预存款
 * <ConfirmDepositCommand>                                //确认存款
 */
@Command
public class DepositTransactionCommandHandle {
    @Subscribe
    public void handleAsync(ICommandContext context, StartDepositTransactionCommand command) {
        context.addAsync(new DepositTransaction(command.getAggregateRootId(), command.AccountId, command.Amount));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmDepositPreparationCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        DepositTransaction depositTransaction = Task.get(future);
        depositTransaction.ConfirmDepositPreparation();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmDepositCommand command) {
        CompletableFuture<DepositTransaction> future = context.getAsync(command.getAggregateRootId(), DepositTransaction.class);
        DepositTransaction depositTransaction = Task.get(future);
        depositTransaction.ConfirmDeposit();
    }
}