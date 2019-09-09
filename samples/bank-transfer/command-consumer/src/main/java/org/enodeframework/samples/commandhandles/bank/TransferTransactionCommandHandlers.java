package org.enodeframework.samples.commandhandles.bank;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.common.io.Task;
import org.enodeframework.samples.commands.bank.CancelTransferTransactionCommand;
import org.enodeframework.samples.commands.bank.ConfirmAccountValidatePassedCommand;
import org.enodeframework.samples.commands.bank.ConfirmTransferInCommand;
import org.enodeframework.samples.commands.bank.ConfirmTransferInPreparationCommand;
import org.enodeframework.samples.commands.bank.ConfirmTransferOutCommand;
import org.enodeframework.samples.commands.bank.ConfirmTransferOutPreparationCommand;
import org.enodeframework.samples.commands.bank.StartTransferTransactionCommand;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferTransaction;

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
        TransferTransaction transaction = Task.await(future);
        transaction.ConfirmAccountValidatePassed(command.AccountId);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmTransferOutPreparationCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.await(future);
        transaction.ConfirmTransferOutPreparation();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmTransferInPreparationCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.await(future);
        transaction.ConfirmTransferInPreparation();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmTransferOutCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.await(future);
        transaction.ConfirmTransferOut();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmTransferInCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.await(future);
        transaction.ConfirmTransferIn();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CancelTransferTransactionCommand command) {
        CompletableFuture<TransferTransaction> future = context.getAsync(command.getAggregateRootId(), TransferTransaction.class);
        TransferTransaction transaction = Task.await(future);
        transaction.Cancel();
    }
}