package com.enode.samples.domain.bank.transfertransaction;

/// <summary>值对象，包含了一次转账交易的基本信息
/// </summary>
public class TransferTransactionInfo {
    /// <summary>源账户
    /// </summary>
    public String SourceAccountId;
    /// <summary>目标账户
    /// </summary>
    public String TargetAccountId;
    /// <summary>转账金额
    /// </summary>
    public double Amount;

    public TransferTransactionInfo(String sourceAccountId, String targetAccountId, double amount) {
        SourceAccountId = sourceAccountId;
        TargetAccountId = targetAccountId;
        Amount = amount;
    }
}