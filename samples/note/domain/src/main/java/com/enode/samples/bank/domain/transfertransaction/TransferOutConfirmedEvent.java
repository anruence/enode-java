package com.enode.samples.bank.domain.transfertransaction;

public class TransferOutConfirmedEvent extends AbstractTransferTransactionEvent {
    public TransferOutConfirmedEvent() {
    }

    public TransferOutConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
