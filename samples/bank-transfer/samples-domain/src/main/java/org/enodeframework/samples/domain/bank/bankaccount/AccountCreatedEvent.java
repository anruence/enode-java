package org.enodeframework.samples.domain.bank.bankaccount;

import org.enodeframework.eventing.DomainEvent;

/**
 * 已开户
 */
public class AccountCreatedEvent extends DomainEvent<String> {
    /**
     * 账户拥有者
     */
    public String owner;

    public AccountCreatedEvent() {
    }

    public AccountCreatedEvent(String owner) {
        this.owner = owner;
    }
}
