package org.enodeframework.samples.domain.bank.deposittransaction;

import org.enodeframework.domain.AggregateRoot;
import org.enodeframework.samples.domain.bank.TransactionStatus;

/**
 * 聚合根，表示一笔银行存款交易
 */
public class DepositTransaction extends AggregateRoot<String> {
    private String accountId;
    private double amount;
    private int status;

    public DepositTransaction() {
    }

    /**
     * 构造函数
     */
    public DepositTransaction(String transactionId, String accountId, double amount) {
        super(transactionId);
        applyEvent(new DepositTransactionStartedEvent(accountId, amount));
    }

    /**
     * 确认预存款
     */
    public void confirmDepositPreparation() {
        if (status == TransactionStatus.STARTED) {
            applyEvent(new DepositTransactionPreparationCompletedEvent(accountId));
        }
    }

    /**
     * 确认存款
     */
    public void confirmDeposit() {
        if (status == TransactionStatus.PREPARATION_COMPLETED) {
            applyEvent(new DepositTransactionCompletedEvent(accountId));
        }
    }

    private void handle(DepositTransactionStartedEvent evnt) {
        accountId = evnt.accountId;
        amount = evnt.amount;
        status = TransactionStatus.STARTED;
    }

    private void handle(DepositTransactionPreparationCompletedEvent evnt) {
        status = TransactionStatus.PREPARATION_COMPLETED;
    }

    private void handle(DepositTransactionCompletedEvent evnt) {
        status = TransactionStatus.COMPLETED;
    }
}
