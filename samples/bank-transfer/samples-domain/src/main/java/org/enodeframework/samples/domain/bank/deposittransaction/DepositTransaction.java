package org.enodeframework.samples.domain.bank.deposittransaction;

import org.enodeframework.domain.AggregateRoot;
import org.enodeframework.samples.domain.bank.TransactionStatus;

/**
 * 聚合根，表示一笔银行存款交易
 */
public class DepositTransaction extends AggregateRoot<String> {
    private String _accountId;
    private double _amount;
    private int _status;

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
    public void ConfirmDepositPreparation() {
        if (_status == TransactionStatus.Started) {
            applyEvent(new DepositTransactionPreparationCompletedEvent(_accountId));
        }
    }

    /**
     * 确认存款
     */
    public void ConfirmDeposit() {
        if (_status == TransactionStatus.PreparationCompleted) {
            applyEvent(new DepositTransactionCompletedEvent(_accountId));
        }
    }

    private void Handle(DepositTransactionStartedEvent evnt) {
        _accountId = evnt.AccountId;
        _amount = evnt.Amount;
        _status = TransactionStatus.Started;
    }

    private void Handle(DepositTransactionPreparationCompletedEvent evnt) {
        _status = TransactionStatus.PreparationCompleted;
    }

    private void Handle(DepositTransactionCompletedEvent evnt) {
        _status = TransactionStatus.Completed;
    }
}
