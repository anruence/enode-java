package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.domain.DomainException;

import java.util.Map;

public class InsufficientBalanceException extends DomainException {
    /**
     * 账户ID
     */
    public String accountId;
    /**
     * 交易ID
     */
    public String transactionId;
    /**
     * 交易类型
     */
    public int transactionType;
    /**
     * 交易金额
     */
    public double amount;
    /**
     * 当前余额
     */
    public double currentBalance;
    /**
     * 当前可用余额
     */
    public double currentAvailableBalance;

    public InsufficientBalanceException(String accountId, String transactionId, int transactionType, double amount, double currentBalance, double currentAvailableBalance) {
        super();
        this.accountId = accountId;
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currentBalance = currentBalance;
        this.currentAvailableBalance = currentAvailableBalance;
    }

    @Override
    public void serializeTo(Map<String, Object> serializableInfo) {
        serializableInfo.put("AccountId", accountId);
        serializableInfo.put("TransactionId", transactionId);
        serializableInfo.put("TransactionType", "");
        serializableInfo.put("Amount", String.valueOf(amount));
        serializableInfo.put("CurrentBalance", String.valueOf(currentBalance));
        serializableInfo.put("CurrentAvailableBalance", String.valueOf(currentAvailableBalance));
    }

    @Override
    public void restoreFrom(Map<String, Object> serializableInfo) {
        accountId = (String) serializableInfo.get("AccountId");
        transactionId = (String) serializableInfo.get("TransactionId");
        transactionType = (Integer) serializableInfo.get("transactionType");
        amount = (Double) (serializableInfo.get("Amount"));
        currentBalance = (Double) (serializableInfo.get("CurrentBalance"));
        currentAvailableBalance = (Double) (serializableInfo.get("CurrentAvailableBalance"));
    }
}
