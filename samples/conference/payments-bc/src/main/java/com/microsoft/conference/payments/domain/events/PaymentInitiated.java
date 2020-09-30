package com.microsoft.conference.payments.domain.events;

import com.microsoft.conference.payments.domain.models.PaymentItem;
import org.enodeframework.eventing.DomainEvent;

import java.math.BigDecimal;
import java.util.List;

public class PaymentInitiated extends DomainEvent<String> {
    public String orderId;
    public String conferenceId;
    public String description;
    public BigDecimal totalAmount;
    public List<PaymentItem> paymentItems;

    public PaymentInitiated() {
    }

    public PaymentInitiated(String orderId, String conferenceId, String description, BigDecimal totalAmount, List<PaymentItem> paymentItems) {
        this.orderId = orderId;
        this.conferenceId = conferenceId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.paymentItems = paymentItems;
    }
}
