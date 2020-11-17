package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.samples.domain.bank.TransactionType;

/**
 * 实体，表示账户聚合内的一笔预操作（如预存款、预取款、预转入、预转出）
 */
public class TransactionPreparation {
    public TransactionPreparation() {

    }

    /**
     * 账户ID
     */
    public String accountId;
    /**
     * 交易ID
     */
    public String transactionId;
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
    public double amount;

    public TransactionPreparation(String accountId, String transactionId, int transactionType, int preparationType, double amount) {
        if (transactionType == TransactionType.TRANSFER_TRANSACTION && preparationType != PreparationType.CREDIT_PREPARATION) {
            throw new MismatchTransactionPreparationException(transactionType, preparationType);
        }
        if (transactionType == TransactionType.WITHDRAW_TRANSACTION && preparationType != PreparationType.DEBIT_PREPARATION) {
            throw new MismatchTransactionPreparationException(transactionType, preparationType);
        }
        this.accountId = accountId;
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.preparationType = preparationType;
        this.amount = amount;
    }
}
