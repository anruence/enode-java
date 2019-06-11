package com.enodeframework.samples.eventhandlers.bank.processmanagers;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.commanding.ICommandService;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.Task;
import com.enodeframework.samples.commands.bank.AddTransactionPreparationCommand;
import com.enodeframework.samples.commands.bank.CommitTransactionPreparationCommand;
import com.enodeframework.samples.commands.bank.ConfirmDepositCommand;
import com.enodeframework.samples.commands.bank.ConfirmDepositPreparationCommand;
import com.enodeframework.samples.domain.bank.TransactionType;
import com.enodeframework.samples.domain.bank.bankaccount.PreparationType;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import com.enodeframework.samples.domain.bank.deposittransaction.DepositTransactionPreparationCompletedEvent;
import com.enodeframework.samples.domain.bank.deposittransaction.DepositTransactionStartedEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 银行存款交易流程管理器，用于协调银行存款交易流程中各个参与者聚合根之间的消息交互
 * IMessageHandler<DepositTransactionStartedEvent>,                    //存款交易已开始
 * IMessageHandler<DepositTransactionPreparationCompletedEvent>,       //存款交易已提交
 * IMessageHandler<TransactionPreparationAddedEvent>,                  //账户预操作已添加
 * IMessageHandler<TransactionPreparationCommittedEvent>               //账户预操作已提交
 */
@Event
public class DepositTransactionProcessManager {

    @Autowired
    private ICommandService _commandService;

    @Subscribe
    public AsyncTaskResult handleAsync(DepositTransactionStartedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.AccountId,
                evnt.aggregateRootId(),
                TransactionType.DepositTransaction,
                PreparationType.CreditPreparation,
                evnt.Amount);
        command.setId(evnt.id());
        return Task.get(_commandService.sendAsync(command));
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.DepositTransaction
                && evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {

            ConfirmDepositPreparationCommand command = new ConfirmDepositPreparationCommand(evnt.TransactionPreparation.TransactionId);
            command.setId(evnt.id());
            return Task.get(_commandService.sendAsync(command));
        }
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(DepositTransactionPreparationCompletedEvent evnt) {
        CommitTransactionPreparationCommand command = new CommitTransactionPreparationCommand(evnt.AccountId, evnt.aggregateRootId());
        command.setId(evnt.id());
        return Task.get(_commandService.sendAsync(command));
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransactionPreparationCommittedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.DepositTransaction &&
                evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
            ConfirmDepositCommand command = new ConfirmDepositCommand(evnt.TransactionPreparation.TransactionId);
            command.setId(evnt.id());
            return Task.get(_commandService.sendAsync(command));
        }
        return (AsyncTaskResult.Success);
    }
}
