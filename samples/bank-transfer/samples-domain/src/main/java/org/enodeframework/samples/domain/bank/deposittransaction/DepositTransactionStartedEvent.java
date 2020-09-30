package org.enodeframework.samples.domain.bank.deposittransaction;

import org.enodeframework.eventing.DomainEvent;

public class DepositTransactionStartedEvent extends DomainEvent<String> {
    public String accountId;
    public double amount;

    public DepositTransactionStartedEvent() {
    }

    public DepositTransactionStartedEvent(String accountId, double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }
}
