package com.enode.samples.bank.domain.bankaccount;

import com.enode.domain.AggregateRoot;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/// <summary>银行账户聚合根，封装银行账户余额变动的数据一致性
/// </summary>
public class BankAccount extends AggregateRoot<String> {

    private Map<String, TransactionPreparation> _transactionPreparations;
    private String _owner;
    private double _balance;

    public BankAccount(String account_id, String owner) {
        super(account_id);
        applyEvent(new AccountCreatedEvent(owner));
    }


    /// <summary>添加一笔预操作
    /// </summary>
    public void AddTransactionPreparation(String transaction_id, int transactionType, int preparationType, double amount) {
        double availableBalance = GetAvailableBalance();
        if (preparationType == PreparationType.DebitPreparation && availableBalance < amount) {
            throw new InsufficientBalanceException(_id, transaction_id, transactionType, amount, _balance, availableBalance);
        }

        applyEvent(new TransactionPreparationAddedEvent(new TransactionPreparation(_id, transaction_id, transactionType, preparationType, amount)));
    }

    /// <summary>提交一笔预操作
    /// </summary>
    public void CommitTransactionPreparation(String transactionId) {
        TransactionPreparation transactionPreparation = GetTransactionPreparation(transactionId);
        double currentBalance = _balance;
        if (transactionPreparation.preparationType == PreparationType.DebitPreparation) {
            currentBalance -= transactionPreparation.Amount;
        } else if (transactionPreparation.preparationType == PreparationType.CreditPreparation) {
            currentBalance += transactionPreparation.Amount;
        }
        applyEvent(new TransactionPreparationCommittedEvent(currentBalance, transactionPreparation));
    }

    /// <summary>取消一笔预操作
    /// </summary>
    public void CancelTransactionPreparation(String transactionId) {
        applyEvent(new TransactionPreparationCanceledEvent(GetTransactionPreparation(transactionId)));
    }


    /// <summary>获取当前账户内的一笔预操作，如果预操作不存在，则抛出异常
    /// </summary>
    private TransactionPreparation GetTransactionPreparation(String transactionId) {
        if (_transactionPreparations == null || _transactionPreparations.size() == 0) {
            throw new TransactionPreparationNotExistException(_id, transactionId);
        }
        TransactionPreparation transactionPreparation = _transactionPreparations.get(transactionId);
        if (transactionPreparation == null) {
            throw new TransactionPreparationNotExistException(_id, transactionId);
        }
        return transactionPreparation;
    }

    /// <summary>获取当前账户的可用余额，需要将已冻结的余额计算在内
    /// </summary>
    private double GetAvailableBalance() {
        if (_transactionPreparations == null || _transactionPreparations.size() == 0) {
            return _balance;
        }

        double totalDebitTransactionPreparationAmount = 0;
        for (TransactionPreparation debitTransactionPreparation : _transactionPreparations.values().stream().filter(x -> x.preparationType == PreparationType.DebitPreparation).collect(Collectors.toList())) {
            totalDebitTransactionPreparationAmount += debitTransactionPreparation.Amount;
        }

        return _balance - totalDebitTransactionPreparationAmount;
    }


    private void Handle(AccountCreatedEvent evnt) {
        _owner = evnt.Owner;
        _transactionPreparations = new HashMap<>();
    }

    private void Handle(TransactionPreparationAddedEvent evnt) {
        _transactionPreparations.put(evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation);
    }

    private void Handle(TransactionPreparationCommittedEvent evnt) {
        _transactionPreparations.remove(evnt.TransactionPreparation.TransactionId);
        _balance = evnt.CurrentBalance;
    }

    private void Handle(TransactionPreparationCanceledEvent evnt) {
        _transactionPreparations.remove(evnt.TransactionPreparation.TransactionId);
    }
}
