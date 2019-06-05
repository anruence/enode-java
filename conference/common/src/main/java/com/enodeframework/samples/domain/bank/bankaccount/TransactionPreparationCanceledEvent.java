package com.enodeframework.samples.domain.bank.bankaccount;


import com.enodeframework.eventing.DomainEvent;

/// <summary>账户预操作已取消
/// </summary>
public class TransactionPreparationCanceledEvent extends DomainEvent<String> {
    public TransactionPreparation TransactionPreparation;

    public TransactionPreparationCanceledEvent() {
    }

    public TransactionPreparationCanceledEvent(TransactionPreparation transactionPreparation) {
        TransactionPreparation = transactionPreparation;
    }
}
