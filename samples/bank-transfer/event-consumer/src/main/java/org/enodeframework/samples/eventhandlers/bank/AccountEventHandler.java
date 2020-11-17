package org.enodeframework.samples.eventhandlers.bank;

import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.samples.applicationmessages.AccountValidateFailedMessage;
import org.enodeframework.samples.applicationmessages.AccountValidatePassedMessage;
import org.enodeframework.samples.domain.bank.TransactionType;
import org.enodeframework.samples.domain.bank.bankaccount.AccountCreatedEvent;
import org.enodeframework.samples.domain.bank.bankaccount.InsufficientBalanceException;
import org.enodeframework.samples.domain.bank.bankaccount.PreparationType;
import org.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationAddedEvent;
import org.enodeframework.samples.domain.bank.bankaccount.TransactionPreparationCommittedEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferInPreparationConfirmedEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferOutPreparationConfirmedEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionCanceledEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionCompletedEvent;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Event
public class AccountEventHandler {
    public static Logger logger = LoggerFactory.getLogger(AccountEventHandler.class);

    @Subscribe
    public void handleAsync(AccountCreatedEvent evnt) {
        logger.info("账户已创建，账户：{}，所有者：{}", evnt.getAggregateRootId(), evnt.owner);

    }

    @Subscribe
    public void handleAsync(AccountValidatePassedMessage message) {
        logger.info("账户验证已通过，交易ID：{}，账户：{}", message.transactionId, message.accountId);
    }

    @Subscribe
    public void handleAsync(AccountValidateFailedMessage message) {
        logger.info("无效的银行账户，交易ID：{}，账户：{}，理由：{}", message.transactionId, message.accountId, message.reason);
    }

    @Subscribe
    public void handleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.transactionPreparation.transactionType == TransactionType.TRANSFER_TRANSACTION) {
            if (evnt.transactionPreparation.preparationType == PreparationType.DEBIT_PREPARATION) {
                logger.info("账户预转出成功，交易ID：{}，账户：{}，金额：{}", evnt.transactionPreparation.transactionId, evnt.transactionPreparation.accountId, evnt.transactionPreparation.amount);
            } else if (evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
                logger.info("账户预转入成功，交易ID：{}，账户：{}，金额：{}", evnt.transactionPreparation.transactionId, evnt.transactionPreparation.accountId, evnt.transactionPreparation.amount);
            }
        }

    }

    @Subscribe
    public void handleAsync(TransactionPreparationCommittedEvent evnt) {
        if (evnt.transactionPreparation.transactionType == TransactionType.DEPOSIT_TRANSACTION) {
            if (evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
                logger.info("账户存款已成功，账户：{}，金额：{}，当前余额：{}", evnt.transactionPreparation.accountId, evnt.transactionPreparation.amount, evnt.currentBalance);
            }
        }
        if (evnt.transactionPreparation.transactionType == TransactionType.TRANSFER_TRANSACTION) {
            if (evnt.transactionPreparation.preparationType == PreparationType.DEBIT_PREPARATION) {
                logger.info("账户转出已成功，交易ID：{}，账户：{}，金额：{}，当前余额：{}", evnt.transactionPreparation.transactionId, evnt.transactionPreparation.accountId, evnt.transactionPreparation.amount, evnt.currentBalance);
            }
            if (evnt.transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
                logger.info("账户转入已成功，交易ID：{}，账户：{}，金额：{}，当前余额：{}", evnt.transactionPreparation.transactionId, evnt.transactionPreparation.accountId, evnt.transactionPreparation.amount, evnt.currentBalance);
            }
        }

    }

    @Subscribe
    public void handleAsync(TransferTransactionStartedEvent evnt) {
        logger.info("转账交易已开始，交易ID：{}，源账户：{}，目标账户：{}，转账金额：{}", evnt.getAggregateRootId(), evnt.transferTransactionInfo.sourceAccountId, evnt.transferTransactionInfo.targetAccountId, evnt.transferTransactionInfo.amount);

    }

    @Subscribe
    public void handleAsync(TransferOutPreparationConfirmedEvent evnt) {
        logger.info("预转出确认成功，交易ID：{}，账户：{}", evnt.getAggregateRootId(), evnt.transferTransactionInfo.sourceAccountId);

    }

    @Subscribe
    public void handleAsync(TransferInPreparationConfirmedEvent evnt) {
        logger.info("预转入确认成功，交易ID：{}，账户：{}", evnt.getAggregateRootId(), evnt.transferTransactionInfo.targetAccountId);

    }

    @Subscribe
    public void handleAsync(TransferTransactionCompletedEvent evnt) {
        logger.info("转账交易已完成，交易ID：{}", evnt.getAggregateRootId());

    }

    @Subscribe
    public void handleAsync(InsufficientBalanceException exception) {
        logger.info("账户的余额不足，交易ID：{}，账户：{}，可用余额：{}，转出金额：{}", exception.transactionId, exception.accountId, exception.currentAvailableBalance, exception.amount);

    }

    @Subscribe
    public void handleAsync(TransferTransactionCanceledEvent evnt) {
        logger.info("转账交易已取消，交易ID：{}", evnt.getAggregateRootId());

    }
}
