package com.enode.samples.bank.domain.transfertransaction;

public class TransferTransactionStartedEvent extends AbstractTransferTransactionEvent {
    public TransferTransactionStartedEvent() {
    }

    public TransferTransactionStartedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
