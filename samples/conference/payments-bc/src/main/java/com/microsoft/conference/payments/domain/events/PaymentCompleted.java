package com.microsoft.conference.payments.domain.events;

import com.microsoft.conference.payments.domain.models.Payment;
import org.enodeframework.eventing.DomainEvent;

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
