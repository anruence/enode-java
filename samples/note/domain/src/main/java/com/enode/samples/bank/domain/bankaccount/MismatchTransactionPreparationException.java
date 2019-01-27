package com.enode.samples.bank.domain.bankaccount;

public class MismatchTransactionPreparationException extends RuntimeException {
    public MismatchTransactionPreparationException(int transactionType, int preparationType) {
        super(String.format("Mismatch transaction type [{0}] and preparation type [{1}].", transactionType, preparationType));
    }
}
