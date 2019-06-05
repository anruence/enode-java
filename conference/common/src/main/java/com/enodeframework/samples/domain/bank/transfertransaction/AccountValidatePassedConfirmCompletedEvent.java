package com.enodeframework.samples.domain.bank.transfertransaction;


/// <summary>源账户和目标账户验证通过事件都已确认
/// </summary>
public class AccountValidatePassedConfirmCompletedEvent extends AbstractTransferTransactionEvent {
    public AccountValidatePassedConfirmCompletedEvent() {
    }

    public AccountValidatePassedConfirmCompletedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
