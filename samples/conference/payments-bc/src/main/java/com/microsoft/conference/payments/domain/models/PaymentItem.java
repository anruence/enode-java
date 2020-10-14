package com.microsoft.conference.payments.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentItem {
    private String id;
    private String description;
    private BigDecimal amount;

    public PaymentItem() {
    }

    public PaymentItem(String description, BigDecimal amount) {
        this.id = "";
        this.description = description;
        this.amount = amount;
    }
}
