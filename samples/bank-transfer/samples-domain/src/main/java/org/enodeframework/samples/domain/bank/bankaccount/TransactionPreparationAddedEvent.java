package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.eventing.DomainEvent;

/**
 * 账户预操作已添加
 */
public class TransactionPreparationAddedEvent extends DomainEvent<String> {
    public TransactionPreparation TransactionPreparation;

    public TransactionPreparationAddedEvent() {
    }

    public TransactionPreparationAddedEvent(TransactionPreparation transactionPreparation) {
        TransactionPreparation = transactionPreparation;
    }
}
