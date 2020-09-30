package org.enodeframework.samples.domain.bank.transfertransaction;

import org.enodeframework.domain.AggregateRoot;
import org.enodeframework.samples.domain.bank.TransactionStatus;

/**
 * 聚合根，表示一笔银行内账户之间的转账交易
 */
public class TransferTransaction extends AggregateRoot<String> {
    private TransferTransactionInfo transferTransactionInfo;
    private int status;
    private boolean isSourceAccountValidatePassed;
    private boolean isTargetAccountValidatePassed;
    private boolean isTransferOutPreparationConfirmed;
    private boolean isTransferInPreparationConfirmed;
    private boolean isTransferOutConfirmed;
    private boolean isTransferInConfirmed;

    public TransferTransaction() {
    }

    /**
     * 构造函数
     */
    public TransferTransaction(String transactionId, TransferTransactionInfo transactionInfo) {
        super(transactionId);
        applyEvent(new TransferTransactionStartedEvent(transactionInfo));
    }

    /**
     * 确认账户验证通过
     */
    public void confirmAccountValidatePassed(String accountId) {
        if (status == TransactionStatus.STARTED) {
            if (accountId == transferTransactionInfo.sourceAccountId) {
                if (!isSourceAccountValidatePassed) {
                    applyEvent(new SourceAccountValidatePassedConfirmedEvent(transferTransactionInfo));
                    if (isTargetAccountValidatePassed) {
                        applyEvent(new AccountValidatePassedConfirmCompletedEvent(transferTransactionInfo));
                    }
                }
            } else if (accountId == transferTransactionInfo.targetAccountId) {
                if (!isTargetAccountValidatePassed) {
                    applyEvent(new TargetAccountValidatePassedConfirmedEvent(transferTransactionInfo));
                    if (isSourceAccountValidatePassed) {
                        applyEvent(new AccountValidatePassedConfirmCompletedEvent(transferTransactionInfo));
                    }
                }
            }
        }
    }

    /**
     * 确认预转出
     */
    public void confirmTransferOutPreparation() {
        if (status == TransactionStatus.ACCOUNT_VALIDATE_COMPLETED) {
            if (!isTransferOutPreparationConfirmed) {
                applyEvent(new TransferOutPreparationConfirmedEvent(transferTransactionInfo));
            }
        }
    }

    /**
     * 确认预转入
     */
    public void confirmTransferInPreparation() {
        if (status == TransactionStatus.ACCOUNT_VALIDATE_COMPLETED) {
            if (!isTransferInPreparationConfirmed) {
                applyEvent(new TransferInPreparationConfirmedEvent(transferTransactionInfo));
            }
        }
    }

    /**
     * 确认转出
     */
    public void confirmTransferOut() {
        if (status == TransactionStatus.PREPARATION_COMPLETED) {
            if (!isTransferOutConfirmed) {
                applyEvent(new TransferOutConfirmedEvent(transferTransactionInfo));
                if (isTransferInConfirmed) {
                    applyEvent(new TransferTransactionCompletedEvent());
                }
            }
        }
    }

    /**
     * 确认转入
     */
    public void confirmTransferIn() {
        if (status == TransactionStatus.PREPARATION_COMPLETED) {
            if (!isTransferInConfirmed) {
                applyEvent(new TransferInConfirmedEvent(transferTransactionInfo));
                if (isTransferOutConfirmed) {
                    applyEvent(new TransferTransactionCompletedEvent());
                }
            }
        }
    }

    /**
     * 取消转账交易
     */
    public void cancel() {
        applyEvent(new TransferTransactionCanceledEvent());
    }

    private void handle(TransferTransactionStartedEvent evnt) {
        transferTransactionInfo = evnt.transferTransactionInfo;
        status = TransactionStatus.STARTED;
    }

    private void handle(SourceAccountValidatePassedConfirmedEvent evnt) {
        isSourceAccountValidatePassed = true;
    }

    private void handle(TargetAccountValidatePassedConfirmedEvent evnt) {
        isTargetAccountValidatePassed = true;
    }

    private void handle(AccountValidatePassedConfirmCompletedEvent evnt) {
        status = TransactionStatus.ACCOUNT_VALIDATE_COMPLETED;
    }

    private void handle(TransferOutPreparationConfirmedEvent evnt) {
        isTransferOutPreparationConfirmed = true;
    }

    private void handle(TransferInPreparationConfirmedEvent evnt) {
        isTransferInPreparationConfirmed = true;
        status = TransactionStatus.PREPARATION_COMPLETED;
    }

    private void handle(TransferOutConfirmedEvent evnt) {
        isTransferOutConfirmed = true;
    }

    private void handle(TransferInConfirmedEvent evnt) {
        isTransferInConfirmed = true;
    }

    private void handle(TransferTransactionCompletedEvent evnt) {
        status = TransactionStatus.COMPLETED;
    }

    private void handle(TransferTransactionCanceledEvent evnt) {
        status = TransactionStatus.CANCELED;
    }
}