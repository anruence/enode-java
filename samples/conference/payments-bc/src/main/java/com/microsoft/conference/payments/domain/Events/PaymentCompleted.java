package com.microsoft.conference.payments.domain.Events;

import com.enodeframework.eventing.DomainEvent;
import com.microsoft.conference.payments.domain.Models.Payment;

public class PaymentCompleted extends DomainEvent<String> {
    public String OrderId;
    public String ConferenceId;

    public PaymentCompleted() {
    }

    public PaymentCompleted(Payment payment, String orderId, String conferenceId) {
        OrderId = orderId;
        ConferenceId = conferenceId;
    }
}
