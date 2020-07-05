package com.microsoft.conference.payments.domain.events;

import com.microsoft.conference.payments.domain.models.PaymentItem;
import org.enodeframework.eventing.DomainEvent;

import java.math.BigDecimal;
import java.util.List;

public class PaymentInitiated extends DomainEvent<String> {
    public String OrderId;
    public String ConferenceId;
    public String Description;
    public BigDecimal TotalAmount;
    public List<PaymentItem> Items;

    public PaymentInitiated() {
    }

    public PaymentInitiated(String orderId, String conferenceId, String description, BigDecimal totalAmount, List<PaymentItem> items) {
        OrderId = orderId;
        ConferenceId = conferenceId;
        Description = description;
        TotalAmount = totalAmount;
        Items = items;
    }
}
