package com.enodeframework.samples.eventhandlers.bank;

import com.enodeframework.annotation.Event;
import com.enodeframework.annotation.Subscribe;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import com.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import com.enodeframework.samples.domain.bank.TransactionType;
import com.enodeframework.samples.domain.bank.bankaccount.AccountCreatedEvent;
import com.enodeframework.samples.domain.bank.bankaccount.InsufficientBalanceException;
import com.enodeframework.samples.domain.bank.bankaccount.PreparationType;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import com.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferInPreparationConfirmedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferOutPreparationConfirmedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionCanceledEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionCompletedEvent;
import com.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Event
public class AccountEventHandler {

    public static Logger logger = LoggerFactory.getLogger(AccountEventHandler.class);

    @Subscribe
    public AsyncTaskResult handleAsync(AccountCreatedEvent evnt) {
        logger.info("账户已创建，账户：{}，所有者：{}", evnt.aggregateRootId(), evnt.Owner);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(AccountValidatePassedMessage message) {
        logger.info("账户验证已通过，交易ID：{}，账户：{}", message.TransactionId, message.AccountId);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(AccountValidateFailedMessage message) {
        logger.info("无效的银行账户，交易ID：{}，账户：{}，理由：{}", message.TransactionId, message.AccountId, message.Reason);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.TransferTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.DebitPreparation) {
                logger.info("账户预转出成功，交易ID：{}，账户：{}，金额：{}", evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount);
            } else if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                logger.info("账户预转入成功，交易ID：{}，账户：{}，金额：{}", evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount);
            }
        }
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransactionPreparationCommittedEvent evnt) {
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
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransferTransactionStartedEvent evnt) {
        logger.info("转账交易已开始，交易ID：{}，源账户：{}，目标账户：{}，转账金额：{}", evnt.aggregateRootId(), evnt.TransactionInfo.SourceAccountId, evnt.TransactionInfo.TargetAccountId, evnt.TransactionInfo.Amount);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransferOutPreparationConfirmedEvent evnt) {
        logger.info("预转出确认成功，交易ID：{}，账户：{}", evnt.aggregateRootId(), evnt.TransactionInfo.SourceAccountId);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransferInPreparationConfirmedEvent evnt) {
        logger.info("预转入确认成功，交易ID：{}，账户：{}", evnt.aggregateRootId(), evnt.TransactionInfo.TargetAccountId);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransferTransactionCompletedEvent evnt) {
        logger.info("转账交易已完成，交易ID：{}", evnt.aggregateRootId());
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(InsufficientBalanceException exception) {
        logger.info("账户的余额不足，交易ID：{}，账户：{}，可用余额：{}，转出金额：{}", exception.TransactionId, exception.AccountId, exception.CurrentAvailableBalance, exception.Amount);
        return (AsyncTaskResult.Success);
    }

    @Subscribe
    public AsyncTaskResult handleAsync(TransferTransactionCanceledEvent evnt) {
        logger.info("转账交易已取消，交易ID：{}", evnt.aggregateRootId());
        return (AsyncTaskResult.Success);
    }
}
