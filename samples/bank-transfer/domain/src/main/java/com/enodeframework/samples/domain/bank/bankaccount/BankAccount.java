package com.enodeframework.samples.domain.bank.bankaccount;

import com.enodeframework.domain.AggregateRoot;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/// <summary>银行账户聚合根，封装银行账户余额变动的数据一致性
/// </summary>
public class BankAccount extends AggregateRoot<String> {

    private Map<String, TransactionPreparation> transactionPreparations;
    private String owner;
    private double balance;

    public BankAccount(String accountid, String owner) {
        super(accountid);
        applyEvent(new AccountCreatedEvent(owner));
    }

    public BankAccount() {
    }

    /// <summary>添加一笔预操作
    /// </summary>
    public void AddTransactionPreparation(String transactionid, int transactionType, int preparationType, double amount) {
        double availableBalance = GetAvailableBalance();
        if (preparationType == PreparationType.DebitPreparation && availableBalance < amount) {
            throw new InsufficientBalanceException(id, transactionid, transactionType, amount, balance, availableBalance);
        }

        applyEvent(new TransactionPreparationAddedEvent(new TransactionPreparation(id, transactionid, transactionType, preparationType, amount)));
    }

    /// <summary>提交一笔预操作
    /// </summary>
    public void CommitTransactionPreparation(String transactionId) {
        TransactionPreparation transactionPreparation = GetTransactionPreparation(transactionId);
        double currentBalance = balance;
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
        if (transactionPreparations == null || transactionPreparations.size() == 0) {
            throw new TransactionPreparationNotExistException(id, transactionId);
        }
        TransactionPreparation transactionPreparation = transactionPreparations.get(transactionId);
        if (transactionPreparation == null) {
            throw new TransactionPreparationNotExistException(id, transactionId);
        }
        return transactionPreparation;
    }

    /// <summary>获取当前账户的可用余额，需要将已冻结的余额计算在内
    /// </summary>
    private double GetAvailableBalance() {
        if (transactionPreparations == null || transactionPreparations.size() == 0) {
            return balance;
        }

        double totalDebitTransactionPreparationAmount = 0;
        for (TransactionPreparation debitTransactionPreparation : transactionPreparations.values().stream().filter(x -> x.preparationType == PreparationType.DebitPreparation).collect(Collectors.toList())) {
            totalDebitTransactionPreparationAmount += debitTransactionPreparation.Amount;
        }

        return balance - totalDebitTransactionPreparationAmount;
    }


    private void handle(AccountCreatedEvent evnt) {
        owner = evnt.Owner;
        transactionPreparations = new HashMap<>();
    }

    private void handle(TransactionPreparationAddedEvent evnt) {
        transactionPreparations.put(evnt.TransactionPreparation.TransactionId, evnt.TransactionPreparation);
    }

    private void handle(TransactionPreparationCommittedEvent evnt) {
        transactionPreparations.remove(evnt.TransactionPreparation.TransactionId);
        balance = evnt.CurrentBalance;
    }

    private void handle(TransactionPreparationCanceledEvent evnt) {
        transactionPreparations.remove(evnt.TransactionPreparation.TransactionId);
    }
}
