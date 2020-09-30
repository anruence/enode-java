package com.microsoft.conference.payments.domain.models;

import java.math.BigDecimal;

public class PaymentItem {
    public String id;
    public String description;
    public BigDecimal amount;

    public PaymentItem(String description, BigDecimal amount) {
        this.id = "";
        this.description = description;
        this.amount = amount;
    }
}
