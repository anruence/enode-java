package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.domain.AggregateRoot;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 银行账户聚合根，封装银行账户余额变动的数据一致性
 */
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

    /**
     * 添加一笔预操作
     */
    public void addTransactionPreparation(String transactionId, int transactionType, int preparationType, double amount) {
        double availableBalance = getAvailableBalance();
        if (preparationType == PreparationType.DEBIT_PREPARATION && availableBalance < amount) {
            throw new InsufficientBalanceException(id, transactionId, transactionType, amount, balance, availableBalance);
        }
        applyEvent(new TransactionPreparationAddedEvent(new TransactionPreparation(id, transactionId, transactionType, preparationType, amount)));
    }

    /**
     * 提交一笔预操作
     */
    public void commitTransactionPreparation(String transactionId) {
        TransactionPreparation transactionPreparation = getTransactionPreparation(transactionId);
        double currentBalance = balance;
        if (transactionPreparation.preparationType == PreparationType.DEBIT_PREPARATION) {
            currentBalance -= transactionPreparation.amount;
        } else if (transactionPreparation.preparationType == PreparationType.CREDIT_PREPARATION) {
            currentBalance += transactionPreparation.amount;
        }
        applyEvent(new TransactionPreparationCommittedEvent(currentBalance, transactionPreparation));
    }

    /**
     * 取消一笔预操作
     */
    public void cancelTransactionPreparation(String transactionId) {
        applyEvent(new TransactionPreparationCanceledEvent(getTransactionPreparation(transactionId)));
    }

    /**
     * 获取当前账户内的一笔预操作，如果预操作不存在，则抛出异常
     */
    private TransactionPreparation getTransactionPreparation(String transactionId) {
        if (transactionPreparations == null || transactionPreparations.size() == 0) {
            throw new TransactionPreparationNotExistException(id, transactionId);
        }
        TransactionPreparation transactionPreparation = transactionPreparations.get(transactionId);
        if (transactionPreparation == null) {
            throw new TransactionPreparationNotExistException(id, transactionId);
        }
        return transactionPreparation;
    }

    /**
     * 获取当前账户的可用余额，需要将已冻结的余额计算在内
     */
    private double getAvailableBalance() {
        if (transactionPreparations == null || transactionPreparations.size() == 0) {
            return balance;
        }
        double totalDebitTransactionPreparationAmount = 0;
        for (TransactionPreparation debitTransactionPreparation : transactionPreparations.values().stream().filter(x -> x.preparationType == PreparationType.DEBIT_PREPARATION).collect(Collectors.toList())) {
            totalDebitTransactionPreparationAmount += debitTransactionPreparation.amount;
        }
        return balance - totalDebitTransactionPreparationAmount;
    }

    private void handle(AccountCreatedEvent evnt) {
        owner = evnt.owner;
        transactionPreparations = new HashMap<>();
    }

    private void handle(TransactionPreparationAddedEvent evnt) {
        transactionPreparations.put(evnt.transactionPreparation.transactionId, evnt.transactionPreparation);
    }

    private void handle(TransactionPreparationCommittedEvent evnt) {
        transactionPreparations.remove(evnt.transactionPreparation.transactionId);
        balance = evnt.currentBalance;
    }

    private void handle(TransactionPreparationCanceledEvent evnt) {
        transactionPreparations.remove(evnt.transactionPreparation.transactionId);
    }
}
