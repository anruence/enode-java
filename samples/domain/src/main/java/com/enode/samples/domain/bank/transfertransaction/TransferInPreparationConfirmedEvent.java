package com.enode.samples.domain.bank.transfertransaction;

public class TransferInPreparationConfirmedEvent extends AbstractTransferTransactionEvent {
    public TransferInPreparationConfirmedEvent() {
    }

    public TransferInPreparationConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
