package com.enode.samples.domain.bank.bankaccount;

public class MismatchTransactionPreparationException extends RuntimeException {
    public MismatchTransactionPreparationException(int transactionType, int preparationType) {
        super(String.format("Mismatch transaction type [%s] and preparation type [%s].", transactionType, preparationType));
    }
}
