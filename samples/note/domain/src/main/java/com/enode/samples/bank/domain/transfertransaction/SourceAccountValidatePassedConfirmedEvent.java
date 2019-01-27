package com.enode.samples.bank.domain.transfertransaction;

public class SourceAccountValidatePassedConfirmedEvent extends AbstractTransferTransactionEvent {
    public SourceAccountValidatePassedConfirmedEvent() {
    }

    public SourceAccountValidatePassedConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
