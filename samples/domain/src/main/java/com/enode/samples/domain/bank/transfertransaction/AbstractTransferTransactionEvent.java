package com.enode.samples.domain.bank.transfertransaction;

import com.enode.eventing.DomainEvent;

/// <summary>转账交易已开始
/// </summary>
public abstract class AbstractTransferTransactionEvent extends DomainEvent<String> {

    public TransferTransactionInfo TransactionInfo;

    public AbstractTransferTransactionEvent() {
    }

    public AbstractTransferTransactionEvent(TransferTransactionInfo transactionInfo) {
        TransactionInfo = transactionInfo;
    }
}