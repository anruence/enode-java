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
        logger.info("账户已创建，账户：{}，所有者：{}", evnt.getAggregateRootId(), evnt.Owner);

    }

    @Subscribe
    public void handleAsync(AccountValidatePassedMessage message) {
        logger.info("账户验证已通过，交易ID：{}，账户：{}", message.TransactionId, message.AccountId);

    }

    @Subscribe
    public void handleAsync(AccountValidateFailedMessage message) {
        logger.info("无效的银行账户，交易ID：{}，账户：{}，理由：{}", message.TransactionId, message.AccountId, message.Reason);

    }

    @Subscribe
    public void handleAsync(TransactionPreparationAddedEvent evnt) {
        if (evnt.TransactionPreparation.transactionType == TransactionType.TransferTransaction) {
            if (evnt.TransactionPreparation.preparationType == PreparationType.DebitPreparation) {
                logger.info("账户预转出成功，交易ID：{}，账户：{}，金额：{}", evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount);
            } else if (evnt.TransactionPreparation.preparationType == PreparationType.CreditPreparation) {
                logger.info("账户预转入成功，交易ID：{}，账户：{}，金额：{}", evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation.AccountId, evnt.TransactionPreparation.Amount);
            }
        }

    }

    @Subscribe
    public void handleAsync(TransactionPreparationCommittedEvent evnt) {
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

    }

    @Subscribe
    public void handleAsync(TransferTransactionStartedEvent evnt) {
        logger.info("转账交易已开始，交易ID：{}，源账户：{}，目标账户：{}，转账金额：{}", evnt.getAggregateRootId(), evnt.TransactionInfo.SourceAccountId, evnt.TransactionInfo.TargetAccountId, evnt.TransactionInfo.Amount);

    }

    @Subscribe
    public void handleAsync(TransferOutPreparationConfirmedEvent evnt) {
        logger.info("预转出确认成功，交易ID：{}，账户：{}", evnt.getAggregateRootId(), evnt.TransactionInfo.SourceAccountId);

    }

    @Subscribe
    public void handleAsync(TransferInPreparationConfirmedEvent evnt) {
        logger.info("预转入确认成功，交易ID：{}，账户：{}", evnt.getAggregateRootId(), evnt.TransactionInfo.TargetAccountId);

    }

    @Subscribe
    public void handleAsync(TransferTransactionCompletedEvent evnt) {
        logger.info("转账交易已完成，交易ID：{}", evnt.getAggregateRootId());

    }

    @Subscribe
    public void handleAsync(InsufficientBalanceException exception) {
        logger.info("账户的余额不足，交易ID：{}，账户：{}，可用余额：{}，转出金额：{}", exception.TransactionId, exception.AccountId, exception.CurrentAvailableBalance, exception.Amount);

    }

    @Subscribe
    public void handleAsync(TransferTransactionCanceledEvent evnt) {
        logger.info("转账交易已取消，交易ID：{}", evnt.getAggregateRootId());

    }
}
