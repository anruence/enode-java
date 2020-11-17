package org.enodeframework.samples.eventhandlers.bank.processmanagers;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.io.Task;
import org.enodeframework.samples.commands.bank.AddTransactionPreparationCommand;
import org.enodeframework.samples.commands.bank.CommitTransactionPreparationCommand;
import org.enodeframework.samples.commands.bank.ConfirmDepositCommand;
import org.enodeframework.samples.commands.bank.ConfirmDepositPreparationCommand;
import org.enodeframework.samples.domain.bank.TransactionType;
import org.enodeframework.samples.domain.bank.bankaccount.PreparationType;
import org.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import org.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import org.enodeframework.samples.domain.bank.deposittransaction.DepositTransactionPreparationCompletedEvent;
import org.enodeframework.samples.domain.bank.deposittransaction.DepositTransactionStartedEvent;
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
    private ICommandService commandService;

    @Subscribe
    public void handleAsync(DepositTransactionStartedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.accountId,
                evnt.getAggregateRootId(),
                TransactionType.DEPOSIT_TRANSACTION,
                PreparationType.CREDIT_PREPARATION,
                evnt.amount);
        command.setId(evnt.getId());
        Task.await(commandService.sendAsync(command));
    }

    @Subscribe
    public void handleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.transactionPreparation.transactionType == TransactionType.DEPOSIT_TRANSACTION
                && evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
            ConfirmDepositPreparationCommand command = new ConfirmDepositPreparationCommand(evnt.transactionPreparation.transactionId);
            command.setId(evnt.getId());
            Task.await(commandService.sendAsync(command));
        }
    }

    @Subscribe
    public void handleAsync(DepositTransactionPreparationCompletedEvent evnt) {
        CommitTransactionPreparationCommand command = new CommitTransactionPreparationCommand(evnt.accountId, evnt.getAggregateRootId());
        command.setId(evnt.getId());
        Task.await(commandService.sendAsync(command));
    }

    @Subscribe
    public void handleAsync(TransactionPreparationCommittedEvent evnt) {
        if (evnt.transactionPreparation.transactionType == TransactionType.DEPOSIT_TRANSACTION &&
                evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
            ConfirmDepositCommand command = new ConfirmDepositCommand(evnt.transactionPreparation.transactionId);
            command.setId(evnt.getId());
            Task.await(commandService.sendAsync(command));
        }
    }
}
