package org.enodeframework.samples.domain.bank.deposittransaction;

import org.enodeframework.eventing.DomainEvent;

/// <summary>存款交易已完成
/// </summary>
public class DepositTransactionCompletedEvent extends DomainEvent<String> {
    public String AccountId;

    public DepositTransactionCompletedEvent() {
    }

    public DepositTransactionCompletedEvent(String accountId) {
        AccountId = accountId;
    }
}
