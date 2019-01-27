package com.enode.samples.bank.domain.transfertransaction;

public class TransferInPreparationConfirmedEvent extends AbstractTransferTransactionEvent {
    public TransferInPreparationConfirmedEvent() {
    }

    public TransferInPreparationConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
