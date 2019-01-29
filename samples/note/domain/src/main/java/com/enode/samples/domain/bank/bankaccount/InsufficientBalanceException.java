package com.enode.samples.domain.bank.bankaccount;

import com.enode.infrastructure.PublishableException;

import java.util.Map;

public class InsufficientBalanceException extends PublishableException {
    /// <summary>账户ID
    /// </summary>
    public String AccountId;
    /// <summary>交易ID
    /// </summary>
    public String TransactionId;
    /// <summary>交易类型
    /// </summary>
    public int TransactionType;
    /// <summary>交易金额
    /// </summary>
    public double Amount;
    /// <summary>当前余额
    /// </summary>
    public double CurrentBalance;
    /// <summary>当前可用余额
    /// </summary>
    public double CurrentAvailableBalance;

    public InsufficientBalanceException(String accountId, String transactionId, int transactionType, double amount, double currentBalance, double currentAvailableBalance) {
        super();
        AccountId = accountId;
        TransactionId = transactionId;
        TransactionType = transactionType;
        Amount = amount;
        CurrentBalance = currentBalance;
        CurrentAvailableBalance = currentAvailableBalance;
    }


    @Override
    public void serializeTo(Map<String, String> serializableInfo) {
        serializableInfo.put("AccountId", AccountId);
        serializableInfo.put("TransactionId", TransactionId);
        serializableInfo.put("TransactionType", "");
        serializableInfo.put("Amount", String.valueOf(Amount));
        serializableInfo.put("CurrentBalance", String.valueOf(CurrentBalance));
        serializableInfo.put("CurrentAvailableBalance", String.valueOf(CurrentAvailableBalance));
    }

    @Override
    public void restoreFrom(Map<String, String> serializableInfo) {
        AccountId = serializableInfo.get("AccountId");
        TransactionId = serializableInfo.get("TransactionId");
        TransactionType = Integer.parseInt(serializableInfo.get("transactionType"));
        Amount = Double.parseDouble(serializableInfo.get("Amount"));
        CurrentBalance = Double.parseDouble(serializableInfo.get("CurrentBalance"));
        CurrentAvailableBalance = Double.parseDouble(serializableInfo.get("CurrentAvailableBalance"));
    }

    @Override
    public String getRoutingKey() {
        return AccountId;
    }
}
