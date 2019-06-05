package com.enodeframework.samples.domain.bank.bankaccount;


import com.enodeframework.eventing.DomainEvent;

/// <summary>账户预操作已添加
/// </summary>
public class TransactionPreparationAddedEvent extends DomainEvent<String> {
    public TransactionPreparation TransactionPreparation;


    public TransactionPreparationAddedEvent() {
    }

    public TransactionPreparationAddedEvent(TransactionPreparation transactionPreparation) {
        TransactionPreparation = transactionPreparation;
    }
}
