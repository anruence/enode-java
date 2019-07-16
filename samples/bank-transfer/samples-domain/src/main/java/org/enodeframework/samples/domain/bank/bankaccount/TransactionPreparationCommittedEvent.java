package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.eventing.DomainEvent;

/**
 * 账户预操作已执行
 */
public class TransactionPreparationCommittedEvent extends DomainEvent<String> {
    public double currentBalance;
    public TransactionPreparation transactionPreparation;

    public TransactionPreparationCommittedEvent() {
    }

    public TransactionPreparationCommittedEvent(double currentBalance, TransactionPreparation transactionPreparation) {
        this.currentBalance = currentBalance;
        this.transactionPreparation = transactionPreparation;
    }
}
