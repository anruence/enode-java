package com.enodeframework.samples.domain.bank.bankaccount;


import com.enodeframework.samples.domain.bank.TransactionType;

/// <summary>实体，表示账户聚合内的一笔预操作（如预存款、预取款、预转入、预转出）
/// </summary>
public class TransactionPreparation {
    /// <summary>账户ID
    /// </summary>
    public String AccountId;


    /// <summary>交易ID
    /// </summary>
    public String TransactionId;


    /// <summary>预借或预贷
    /// </summary>
    public int preparationType;
    /// <summary>交易类型
    /// </summary>
    public int transactionType;
    /// <summary>交易金额
    /// </summary>
    public double Amount;


    public TransactionPreparation(String accountId, String transactionId, int transactionType, int preparationType, double amount) {
        if (transactionType == TransactionType.TransferTransaction && preparationType != PreparationType.CreditPreparation) {
            throw new MismatchTransactionPreparationException(transactionType, preparationType);
        }
        if (transactionType == TransactionType.WithdrawTransaction && preparationType != PreparationType.DebitPreparation) {
            throw new MismatchTransactionPreparationException(transactionType, preparationType);
        }
        AccountId = accountId;
        TransactionId = transactionId;
        this.transactionType = transactionType;
        this.preparationType = preparationType;
        Amount = amount;
    }
}
