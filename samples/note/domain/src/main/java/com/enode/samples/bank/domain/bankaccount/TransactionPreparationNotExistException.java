package com.enode.samples.bank.domain.bankaccount;

public class TransactionPreparationNotExistException extends RuntimeException {
    public TransactionPreparationNotExistException(String accountId, String transactionId) {
        super();
    }
}
