package com.enodeframework.samples.domain.bank.transfertransaction;

public class TransferOutPreparationConfirmedEvent extends AbstractTransferTransactionEvent {

    public TransferOutPreparationConfirmedEvent() {
    }

    public TransferOutPreparationConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
