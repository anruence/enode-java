package com.enode.samples.domain.bank.transfertransaction;

public class TransferTransactionStartedEvent extends AbstractTransferTransactionEvent {
    public TransferTransactionStartedEvent() {
    }

    public TransferTransactionStartedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
