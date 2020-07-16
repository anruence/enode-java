package org.enodeframework.samples.domain.bank.transfertransaction;

/**
 * 源账户和目标账户验证通过事件都已确认
 */
public class AccountValidatePassedConfirmCompletedEvent extends AbstractTransferTransactionEvent {
    public AccountValidatePassedConfirmCompletedEvent() {
    }

    public AccountValidatePassedConfirmCompletedEvent(TransferTransactionInfo transactionInfo) {
        super(transactionInfo);
    }
}
