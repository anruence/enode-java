package com.enode.samples.domain.bank.bankaccount;

public class TransactionPreparationNotExistException extends RuntimeException {
    public TransactionPreparationNotExistException(String accountId, String transactionId) {
        super();
    }
}
