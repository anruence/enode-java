﻿package com.enode.samples.bank.domain.bankaccount;

import com.enode.eventing.DomainEvent;

/// <summary>已开户
/// </summary>
public class AccountCreatedEvent extends DomainEvent<String> {
    /// <summary>账户拥有者
    /// </summary>
    public String Owner;

    public AccountCreatedEvent() {
    }

    public AccountCreatedEvent(String owner) {
        Owner = owner;
    }
}
