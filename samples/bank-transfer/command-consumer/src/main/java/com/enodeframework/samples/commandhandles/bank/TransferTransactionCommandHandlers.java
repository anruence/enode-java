package com.enodeframework.samples.commandhandles.bank;

import com.enodeframework.annotation.Command;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.commanding.ICommandContext;
import com.enodeframework.common.io.Task;
import com.enodeframework.samples.commands.bank.CancelTransferTransactionCommand;
import com.enodeframework.samples.commands.bank.ConfirmAccountValidatePassedCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferInCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferInPreparationCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferOutCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferOutPreparationCommand;
import com.enodeframework.samples.commands.bank.StartTransferTransactionCommand;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransaction;

import java.util.concurrent.CompletableFuture;

/**
 * 银行转账交易相关命令处理
 * <StartTransferTransactionCommand>,                       //开始转账交易
 * <ConfirmAccountValidatePassedCommand>,                   //确认账户验证已通过
 * <ConfirmTransferOutPreparationCommand>,                  //确认预转出
 * <ConfirmTransferInPreparationCommand>,                   //确认预转入
 * <ConfirmTransferOutCommand>,                             //确认转出
 * <ConfirmTransferInCommand>,                              //确认转入
 * <CancelTransferTransactionCommand>                       //取消交易
 */
@Command
public class TransferTransactionCommandHandlers {
    @Subscribe
    public void handleAsync(ICommandContext context, StartTransferTransactionCommand command) {
        context.addAsync(new TransferTransaction(command.getAggregateRootId(), command.TransactionInfo));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmAccountValidatePassedCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.get(future);
        transaction.ConfirmAccountValidatePassed(command.AccountId);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmTransferOutPreparationCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.get(future);
        transaction.ConfirmTransferOutPreparation();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmTransferInPreparationCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.get(future);
        transaction.ConfirmTransferInPreparation();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmTransferOutCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.get(future);
        transaction.ConfirmTransferOut();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmTransferInCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.get(future);
        transaction.ConfirmTransferIn();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CancelTransferTransactionCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.get(future);
        transaction.Cancel();
    }
}