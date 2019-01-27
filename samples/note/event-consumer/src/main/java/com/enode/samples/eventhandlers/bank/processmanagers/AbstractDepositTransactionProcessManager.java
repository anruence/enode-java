package com.enode.samples.eventhandlers.bank.processmanagers;
/// <summary>银行存款交易流程管理器，用于协调银行存款交易流程中各个参与者聚合根之间的消息交互。

import com.enode.commanding.ICommandService;
import com.enode.common.io.AsyncTaskResult;
import com.enode.samples.bank.commands.AddTransactionPreparationCommand;
import com.enode.samples.bank.commands.CommitTransactionPreparationCommand;
import com.enode.samples.bank.commands.ConfirmDepositCommand;
import com.enode.samples.bank.commands.ConfirmDepositPreparationCommand;
import com.enode.samples.bank.domain.TransactionType;
import com.enode.samples.bank.domain.bankaccount.PreparationType;
import com.enode.samples.bank.domain.bankaccount.TransactionPreparationAddedEvent;
import com.enode.samples.bank.domain.bankaccount.TransactionPreparationCommittedEvent;
import com.enode.samples.bank.domain.deposittransaction.DepositTransactionPreparationCompletedEvent;
import com.enode.samples.bank.domain.deposittransaction.DepositTransactionStartedEvent;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractDepositTransactionProcessManager {

    private ICommandService _commandService;

    public CompletableFuture<AsyncTaskResult> HandleAsync(DepositTransactionStartedEvent evnt) {
        AddTransactionPreparationCommand command = new AddTransactionPreparationCommand(
                evnt.AccountId,
                evnt.aggregateRootId(),
                TransactionType.DepositTransaction,
                PreparationType.CreditPreparation,
                evnt.Amount);
        command.setId(evnt.id());
        return _commandService.sendAsync(command);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.DepositTransaction
                && evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {

            ConfirmDepositPreparationCommand command = new ConfirmDepositPreparationCommand(evnt.TransactionPreparation.TransactionId);
            command.setId(evnt.id());
            return _commandService.sendAsync(command);
        }
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(DepositTransactionPreparationCompletedEvent evnt) {
        CommitTransactionPreparationCommand command = new CommitTransactionPreparationCommand(evnt.AccountId, evnt.aggregateRootId());
        command.setId(evnt.id());
        return _commandService.sendAsync(command);
    }

    public CompletableFuture<AsyncTaskResult> HandleAsync(TransactionPreparationCommittedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.DepositTransaction &&
                evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
            ConfirmDepositCommand command = new ConfirmDepositCommand(evnt.TransactionPreparation.TransactionId);
            command.setId(evnt.id());
            return _commandService.sendAsync(command);
        }
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}
