package org.enodeframework.samples.domain.bank.transfertransaction;

public class TransferInConfirmedEvent extends AbstractTransferTransactionEvent {
    public TransferInConfirmedEvent() {
    }

    public TransferInConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
