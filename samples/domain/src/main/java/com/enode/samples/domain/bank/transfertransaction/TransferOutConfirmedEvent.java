package com.enode.samples.domain.bank.transfertransaction;

public class TransferOutConfirmedEvent extends AbstractTransferTransactionEvent {
    public TransferOutConfirmedEvent() {
    }

    public TransferOutConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
