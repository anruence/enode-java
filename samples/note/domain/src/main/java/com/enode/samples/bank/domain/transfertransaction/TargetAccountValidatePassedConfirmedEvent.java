package com.enode.samples.bank.domain.transfertransaction;

public class TargetAccountValidatePassedConfirmedEvent extends AbstractTransferTransactionEvent {
    public TargetAccountValidatePassedConfirmedEvent() {
    }

    public TargetAccountValidatePassedConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
