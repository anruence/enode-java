package org.enodeframework.samples.domain.bank.transfertransaction;

public class TargetAccountValidatePassedConfirmedEvent extends AbstractTransferTransactionEvent {
    public TargetAccountValidatePassedConfirmedEvent() {
    }

    public TargetAccountValidatePassedConfirmedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
