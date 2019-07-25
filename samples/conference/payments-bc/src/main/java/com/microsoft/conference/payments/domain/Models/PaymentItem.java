package com.microsoft.conference.payments.domain.Models;

import java.math.BigDecimal;

public class PaymentItem {
    public String Id;
    public String Description;
    public BigDecimal Amount;

    public PaymentItem(String description, BigDecimal amount) {
        this.Id = "";
        this.Description = description;
        this.Amount = amount;
    }
}
