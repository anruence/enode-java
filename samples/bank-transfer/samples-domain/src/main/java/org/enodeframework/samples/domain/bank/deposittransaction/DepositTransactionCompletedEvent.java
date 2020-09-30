package org.enodeframework.samples.domain.bank.deposittransaction;

import org.enodeframework.eventing.DomainEvent;

/**
 * 存款交易已完成
 */
public class DepositTransactionCompletedEvent extends DomainEvent<String> {
    public String accountId;

    public DepositTransactionCompletedEvent() {
    }

    public DepositTransactionCompletedEvent(String accountId) {
        this.accountId = accountId;
    }
}
