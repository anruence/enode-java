package com.enode.samples.eventhandlers.bank;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.logging.ENodeLogger;
import com.enode.samples.applicationmessages.AccountValidateFailedMessage;
import com.enode.samples.applicationmessages.AccountValidatePassedMessage;
import com.enode.samples.domain.bank.TransactionType;
import com.enode.samples.domain.bank.bankaccount.AccountCreatedEvent;
import com.enode.samples.domain.bank.bankaccount.InsufficientBalanceException;
import com.enode.samples.domain.bank.bankaccount.PreparationType;
import com.enode.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import com.enode.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import com.enode.samples.domain.bank.transfertransaction.TransferInPreparationConfirmedEvent;
import com.enode.samples.domain.bank.transfertransaction.TransferOutPreparationConfirmedEvent;
import com.enode.samples.domain.bank.transfertransaction.TransferTransactionCanceledEvent;
import com.enode.samples.domain.bank.transfertransaction.TransferTransactionCompletedEvent;
import com.enode.samples.domain.bank.transfertransaction.TransferTransactionStartedEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class AbstractEventHandler {

    public static Logger logger = ENodeLogger.getLog();

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(AccountCreatedEvent evnt) {
        logger.info("账户已创建，账户：{}，所有者：{}", evnt.aggregateRootId(), evnt.Owner);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(AccountValidatePassedMessage message) {
        logger.info("账户验证已通过，交易ID：{}，账户：{}", message.TransactionId, message.AccountId);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(AccountValidateFailedMessage message) {
        logger.info("无效的银行账户，交易ID：{}，账户：{}，理由：{}", message.TransactionId, message.AccountId, message.Reason);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(TransactionPreparationAddedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.TransferTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.DebitPreparation) {
                logger.info("账户预转出成功，交易ID：{}，账户：{}，金额：{}", evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount);
            } else if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                logger.info("账户预转入成功，交易ID：{}，账户：{}，金额：{}", evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount);
            }
        }
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(TransactionPreparationCommittedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.DepositTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                logger.info("账户存款已成功，账户：{}，金额：{}，当前余额：{}", evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount, evnt.CurrentBalance);
            }
        }
        if (evnt.TransactionPreparation.transactionType == TransactionType.TransferTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.DebitPreparation) {
                logger.info("账户转出已成功，交易ID：{}，账户：{}，金额：{}，当前余额：{}", evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount, evnt.CurrentBalance);
            }
            if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                logger.info("账户转入已成功，交易ID：{}，账户：{}，金额：{}，当前余额：{}", evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount, evnt.CurrentBalance);
            }
        }
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(TransferTransactionStartedEvent evnt) {
        logger.info("转账交易已开始，交易ID：{}，源账户：{}，目标账户：{}，转账金额：{}", evnt.aggregateRootId(), evnt.TransactionInfo.SourceAccountId, evnt.TransactionInfo.TargetAccountId, evnt.TransactionInfo.Amount);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(TransferOutPreparationConfirmedEvent evnt) {
        logger.info("预转出确认成功，交易ID：{}，账户：{}", evnt.aggregateRootId(), evnt.TransactionInfo.SourceAccountId);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(TransferInPreparationConfirmedEvent evnt) {
        logger.info("预转入确认成功，交易ID：{}，账户：{}", evnt.aggregateRootId(), evnt.TransactionInfo.TargetAccountId);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(TransferTransactionCompletedEvent evnt) {
        logger.info("转账交易已完成，交易ID：{}", evnt.aggregateRootId());
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(InsufficientBalanceException exception) {
        logger.info("账户的余额不足，交易ID：{}，账户：{}，可用余额：{}，转出金额：{}", exception.TransactionId, exception.AccountId, exception.CurrentAvailableBalance, exception.Amount);
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    public CompletableFuture<AsyncTaskResult> handleAsyncInternal(TransferTransactionCanceledEvent evnt) {
        logger.info("转账交易已取消，交易ID：{}", evnt.aggregateRootId());
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}
