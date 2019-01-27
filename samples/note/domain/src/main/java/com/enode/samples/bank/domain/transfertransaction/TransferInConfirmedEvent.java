package com.enode.samples.bank.domain.transfertransaction;

public class TransferInConfirmedEvent extends AbstractTransferTransactionEvent {
    public TransferInConfirmedEvent() {
    }

    public TransferInConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
