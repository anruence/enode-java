package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.eventing.DomainEvent;

/**
 * 账户预操作已取消
 */
public class TransactionPreparationCanceledEvent extends DomainEvent<String> {
    public TransactionPreparation transactionPreparation;

    public TransactionPreparationCanceledEvent() {
    }

    public TransactionPreparationCanceledEvent(TransactionPreparation transactionPreparation) {
        this.transactionPreparation = transactionPreparation;
    }
}
