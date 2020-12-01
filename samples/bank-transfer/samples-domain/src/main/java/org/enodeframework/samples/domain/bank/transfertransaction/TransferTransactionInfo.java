package org.enodeframework.samples.domain.bank.transfertransaction;

/**
 * 值对象，包含了一次转账交易的基本信息
 */
public class TransferTransactionInfo {

    /**
     * 源账户
     */
    public String sourceAccountId;
    /**
     * 目标账户
     */
    public String targetAccountId;
    /**
     * 转账金额
     */
    public double amount;

    public TransferTransactionInfo() {

    }

    public TransferTransactionInfo(String sourceAccountId, String targetAccountId, double amount) {
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
    }
}