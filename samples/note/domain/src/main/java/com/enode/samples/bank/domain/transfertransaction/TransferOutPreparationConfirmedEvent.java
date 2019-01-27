package com.enode.samples.bank.domain.transfertransaction;

public class TransferOutPreparationConfirmedEvent extends AbstractTransferTransactionEvent {

    public TransferOutPreparationConfirmedEvent() {
    }

    public TransferOutPreparationConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
