package com.microsoft.conference.payments.domain.events;

import com.microsoft.conference.payments.domain.models.Payment;
import org.enodeframework.eventing.DomainEvent;

public class PaymentCompleted extends DomainEvent<String> {
    public String orderId;
    public String conferenceId;

    public PaymentCompleted() {
    }

    public PaymentCompleted(Payment payment, String orderId, String conferenceId) {
        this.orderId = orderId;
        this.conferenceId = conferenceId;
    }
}
