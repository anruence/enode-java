package com.enode.samples.domain.bank.deposittransaction;

import com.enode.domain.AggregateRoot;
import com.enode.samples.domain.bank.TransactionStatus;

/// <summary>聚合根，表示一笔银行存款交易
/// </summary>
public class DepositTransaction extends AggregateRoot<String> {

    private String _accountId;
    private double _amount;
    private int _status;


    /// <summary>构造函数
    /// </summary>
    public DepositTransaction(String transactionId, String accountId, double amount) {
        super(transactionId);
        applyEvent(new DepositTransactionStartedEvent(accountId, amount));
    }


    /// <summary>确认预存款
    /// </summary>
    public void ConfirmDepositPreparation() {
        if (_status == TransactionStatus.Started) {
            applyEvent(new DepositTransactionPreparationCompletedEvent(_accountId));
        }
    }

    /// <summary>确认存款
    /// </summary>
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
