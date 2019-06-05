package com.enodeframework.samples.domain.bank.transfertransaction;

public class SourceAccountValidatePassedConfirmedEvent extends AbstractTransferTransactionEvent {
    public SourceAccountValidatePassedConfirmedEvent() {
    }

    public SourceAccountValidatePassedConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
