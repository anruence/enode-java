package com.enodeframework.samples.eventhandlers.bank.processmanagers;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.commanding.ICommandService;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.Task;
import com.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import com.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import com.enodeframework.samples.commands.bank.AddTransactionPreparationCommand;
import com.enodeframework.samples.commands.bank.CancelTransferTransactionCommand;
import com.enodeframework.samples.commands.bank.CommitTransactionPreparationCommand;
import com.enodeframework.samples.commands.bank.ConfirmAccountValidatePassedCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferInCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferInPreparationCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferOutCommand;
import com.enodeframework.samples.commands.bank.ConfirmTransferOutPreparationCommand;
import com.enodeframework.samples.commands.bank.ValidateAccountCommand;
import com.enodeframework.samples.domain.bank.TransactionType;
import com.enodeframework.samples.domain.bank.bankaccount.InsufficientBalanceException;
import com.enodeframework.samples.domain.bank.bankaccount.PreparationType;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.AccountValidatePassedConfirmCompletedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferInPreparationConfirmedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferOutPreparationConfirmedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionStartedEvent;
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
    private ICommandService _commandService;

    @Subscribe
    public AsyncTaskResult handleAsync(TransferTransactionStartedEvent evnt) {
        ValidateAccountCommand command = new ValidateAccountCommand(evnt.TransactionInfo.SourceAccountId, evnt.aggregateRootId());
        command.setId(evnt.id());
        ValidateAccountCommand targetCommand = new ValidateAccountCommand(evnt.TransactionInfo.TargetAccountId, evnt.aggregateRootId());
        targetCommand.setId(evnt.id());
        CompletableFuture task1 = _commandService.sendAsync(command);
        CompletableFuture task2 = _commandService.sendAsync(targetCommand);
        CompletableFuture.allOf(task1, task2);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(AccountValidatePassedMessage message) {
        ConfirmAccountValidatePassedCommand command = new ConfirmAccountValidatePassedCommand(message.TransactionId, message.AccountId);
        command.setId(message.id());
        return Task.get(_commandService.sendAsync(command));

    }

    @Subscribe
    public AsyncTaskResult handleAsync(AccountValidateFailedMessage message) {
        CancelTransferTransactionCommand command = new CancelTransferTransactionCommand(message.TransactionId);
        command.setId(message.id());
        return Task.get(_commandService.sendAsync(command));
    }

    @Subscribe
    public AsyncTaskResult handleAsync(AccountValidatePassedConfirmCompletedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.TransactionInfo.SourceAccountId,
                evnt.aggregateRootId(),
                TransactionType.TransferTransaction,
                PreparationType.DebitPreparation,
                evnt.TransactionInfo.Amount);
        command.setId(evnt.id());
        return Task.get(_commandService.sendAsync(command));

    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.TransferTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.DebitPreparation) {
                ConfirmTransferOutPreparationCommand command = new ConfirmTransferOutPreparationCommand(evnt.TransactionPreparation.TransactionId);
                command.setId(evnt.id());
                return Task.get(_commandService.sendAsync(command));
            } else if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                ConfirmTransferInPreparationCommand command = new ConfirmTransferInPreparationCommand(evnt.TransactionPreparation.TransactionId);
                command.setId(evnt.id());
                return Task.get(_commandService.sendAsync(command));
            }
        }
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(InsufficientBalanceException exception) {
        if (exception.TransactionType == TransactionType.TransferTransaction) {
            CancelTransferTransactionCommand command = new CancelTransferTransactionCommand(exception.TransactionId);
            command.setId(exception.id());
            return Task.get(_commandService.sendAsync(command));
        }
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransferOutPreparationConfirmedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.TransactionInfo.TargetAccountId,
                evnt.aggregateRootId(),
                TransactionType.TransferTransaction,
                PreparationType.CreditPreparation,
                evnt.TransactionInfo.Amount);
        command.setId(evnt.id());
        return Task.get(_commandService.sendAsync(command));
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransferInPreparationConfirmedEvent evnt) {
        CommitTransactionPreparationCommand command = new CommitTransactionPreparationCommand(evnt.TransactionInfo.SourceAccountId, evnt.aggregateRootId());
        command.setId(evnt.id());
        CommitTransactionPreparationCommand targetCommand = new CommitTransactionPreparationCommand(evnt.TransactionInfo.TargetAccountId, evnt.aggregateRootId());
        targetCommand.setId(evnt.id());
        CompletableFuture task1 = _commandService.sendAsync(command);
        CompletableFuture task2 = _commandService.sendAsync(targetCommand);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransactionPreparationCommittedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.TransferTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.DebitPreparation) {
                ConfirmTransferOutCommand command = new ConfirmTransferOutCommand(evnt.TransactionPreparation.TransactionId);
                command.setId(evnt.id());
                return Task.get(_commandService.sendAsync(command));
            } else if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                ConfirmTransferInCommand command = new ConfirmTransferInCommand(evnt.TransactionPreparation.TransactionId);
                command.setId(evnt.id());
                return Task.get(_commandService.sendAsync(command));
            }
        }
        return (AsyncTaskResult.Success);
    }
}
