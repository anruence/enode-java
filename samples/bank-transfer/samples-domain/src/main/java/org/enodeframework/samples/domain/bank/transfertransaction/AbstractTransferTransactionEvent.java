package org.enodeframework.samples.domain.bank.transfertransaction;

import org.enodeframework.eventing.DomainEvent;

/**
 * 转账交易已开始
 */
public abstract class AbstractTransferTransactionEvent extends DomainEvent<String> {
    public TransferTransactionInfo transferTransactionInfo;

    public AbstractTransferTransactionEvent() {
    }

    public AbstractTransferTransactionEvent(TransferTransactionInfo transferTransactionInfo) {
        this.transferTransactionInfo = transferTransactionInfo;
    }
}