package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.samples.domain.bank.TransactionType;

/**
 * 实体，表示账户聚合内的一笔预操作（如预存款、预取款、预转入、预转出）
 */
public class TransactionPreparation {
    /**
     * 账户ID
     */
    public String AccountId;
    /**
     * 交易ID
     */
    public String TransactionId;
    /**
     * 预借或预贷
     */
    public int preparationType;
    /**
     * 交易类型
     */
    public int transactionType;
    /**
     * 交易金额
     */
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
