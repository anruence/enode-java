package org.enodeframework.samples.domain.bank.transfertransaction;

/**
 * 值对象，包含了一次转账交易的基本信息
 */
public class TransferTransactionInfo {

    public TransferTransactionInfo() {

    }

    /**
     * 源账户
     */
    public String SourceAccountId;
    /**
     * 目标账户
     */
    public String TargetAccountId;
    /**
     * 转账金额
     */
    public double Amount;

    public TransferTransactionInfo(String sourceAccountId, String targetAccountId, double amount) {
        SourceAccountId = sourceAccountId;
        TargetAccountId = targetAccountId;
        Amount = amount;
    }
}