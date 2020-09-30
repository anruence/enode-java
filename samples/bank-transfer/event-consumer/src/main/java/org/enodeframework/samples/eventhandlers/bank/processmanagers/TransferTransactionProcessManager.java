package org.enodeframework.samples.eventhandlers.bank.processmanagers;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.io.Task;
import org.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import org.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import org.enodeframework.samples.commands.bank.AddTransactionPreparationCommand;
import org.enodeframework.samples.commands.bank.CancelTransferTransactionCommand;
import org.enodeframework.samples.commands.bank.CommitTransactionPreparationCommand;
import org.enodeframework.samples.commands.bank.ConfirmAccountValidatePassedCommand;
import org.enodeframework.samples.commands.bank.ConfirmTransferInCommand;
import org.enodeframework.samples.commands.bank.ConfirmTransferInPreparationCommand;
import org.enodeframework.samples.commands.bank.ConfirmTransferOutCommand;
import org.enodeframework.samples.commands.bank.ConfirmTransferOutPreparationCommand;
import org.enodeframework.samples.commands.bank.ValidateAccountCommand;
import org.enodeframework.samples.domain.bank.TransactionType;
import org.enodeframework.samples.domain.bank.bankaccount.InsufficientBalanceException;
import org.enodeframework.samples.domain.bank.bankaccount.PreparationType;
import org.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import org.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.AccountValidatePassedConfirmCompletedEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferInPreparationConfirmedEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferOutPreparationConfirmedEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionStartedEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * IMessageHandler<TransferTransactionStartedEvent>,                  //转账交易已开始
 * IMessageHandler<AccountValidatePassedMessage>,                     //账户验证已通过
 * IMessageHandler<AccountValidateFailedMessage>,                     //账户验证未通过
 * IMessageHandler<AccountValidatePassedConfirmCompletedEvent>,       //两个账户的验证通过事件都已确认
 * IMessageHandler<TransactionPreparationAddedEvent>,                 //账户预操作已添加
 * IMessageHandler<InsufficientBalanceException>,                     //账户余额不足
 * IMessageHandler<TransferOutPreparationConfirmedEvent>,             //转账交易预转出已确认
 * IMessageHandler<TransferInPreparationConfirmedEvent>,              //转账交易预转入已确认
 * IMessageHandler<TransactionPreparationCommittedEvent>              //账户预操作已提交
 */
@Event
public class TransferTransactionProcessManager {

    @Autowired
    private ICommandService commandService;

    @Subscribe
    public void handleAsync(TransferTransactionStartedEvent evnt) {
        ValidateAccountCommand command = new ValidateAccountCommand(evnt.transferTransactionInfo.sourceAccountId, evnt.getAggregateRootId());
        command.setId(evnt.getId());
        ValidateAccountCommand targetCommand = new ValidateAccountCommand(evnt.transferTransactionInfo.targetAccountId, evnt.getAggregateRootId());
        targetCommand.setId(evnt.getId());
        CompletableFuture<Void> task1 = commandService.sendAsync(command);
        CompletableFuture<Void> task2 = commandService.sendAsync(targetCommand);
        Task.await(CompletableFuture.allOf(task1, task2));
    }

    @Subscribe
    public void handleAsync(AccountValidatePassedMessage message) {
        ConfirmAccountValidatePassedCommand command = new ConfirmAccountValidatePassedCommand(message.transactionId, message.accountId);
        command.setId(message.getId());
        Task.await(commandService.sendAsync(command));
    }

    @Subscribe
    public void handleAsync(AccountValidateFailedMessage message) {
        CancelTransferTransactionCommand command = new CancelTransferTransactionCommand(message.transactionId);
        command.setId(message.getId());
        Task.await(commandService.sendAsync(command));
    }

    @Subscribe
    public void handleAsync(AccountValidatePassedConfirmCompletedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.transferTransactionInfo.sourceAccountId,
                evnt.getAggregateRootId(),
                TransactionType.TRANSFER_TRANSACTION,
                PreparationType.DEBIT_PREPARATION,
                evnt.transferTransactionInfo.amount);
        command.setId(evnt.getId());
        Task.await(commandService.sendAsync(command));
    }

    @Subscribe
    public void handleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.transactionPreparation.transactionType == TransactionType.TRANSFER_TRANSACTION) {
            if (evnt.transactionPreparation.preparationType == PreparationType.DEBIT_PREPARATION) {
                ConfirmTransferOutPreparationCommand command = new ConfirmTransferOutPreparationCommand(evnt.transactionPreparation.TransactionId);
                command.setId(evnt.getId());
                Task.await(commandService.sendAsync(command));
            } else if (evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
                ConfirmTransferInPreparationCommand command = new ConfirmTransferInPreparationCommand(evnt.transactionPreparation.TransactionId);
                command.setId(evnt.getId());
                Task.await(commandService.sendAsync(command));
            }
        }
    }

    @Subscribe
    public void handleAsync(InsufficientBalanceException exception) {
        if (exception.transactionType == TransactionType.TRANSFER_TRANSACTION) {
            CancelTransferTransactionCommand command = new CancelTransferTransactionCommand(exception.transactionId);
            command.setId(exception.getId());
            Task.await(commandService.sendAsync(command));
        }
    }

    @Subscribe
    public void handleAsync(TransferOutPreparationConfirmedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.transferTransactionInfo.targetAccountId,
                evnt.getAggregateRootId(),
                TransactionType.TRANSFER_TRANSACTION,
                PreparationType.CREDIT_PREPARATION,
                evnt.transferTransactionInfo.amount);
        command.setId(evnt.getId());
        Task.await(commandService.sendAsync(command));
    }

    @Subscribe
    public void handleAsync(TransferInPreparationConfirmedEvent evnt) {
        CommitTransactionPreparationCommand command = new CommitTransactionPreparationCommand(evnt.transferTransactionInfo.sourceAccountId, evnt.getAggregateRootId());
        command.setId(evnt.getId());
        CommitTransactionPreparationCommand targetCommand = new CommitTransactionPreparationCommand(evnt.transferTransactionInfo.targetAccountId, evnt.getAggregateRootId());
        targetCommand.setId(evnt.getId());
        CompletableFuture<Void> task1 = commandService.sendAsync(command);
        CompletableFuture<Void> task2 = commandService.sendAsync(targetCommand);
        Task.await(CompletableFuture.allOf(task1, task2));
    }

    @Subscribe
    public void handleAsync(TransactionPreparationCommittedEvent evnt) {
        if (evnt.transactionPreparation.transactionType == TransactionType.TRANSFER_TRANSACTION) {
            if (evnt.transactionPreparation.preparationType == PreparationType.DEBIT_PREPARATION) {
                ConfirmTransferOutCommand command = new ConfirmTransferOutCommand(evnt.transactionPreparation.TransactionId);
                command.setId(evnt.getId());
                Task.await(commandService.sendAsync(command));
            } else if (evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
                ConfirmTransferInCommand command = new ConfirmTransferInCommand(evnt.transactionPreparation.TransactionId);
                command.setId(evnt.getId());
                Task.await(commandService.sendAsync(command));
            }
        }
    }
}
