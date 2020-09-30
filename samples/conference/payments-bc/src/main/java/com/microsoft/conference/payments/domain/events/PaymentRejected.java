package com.microsoft.conference.payments.domain.events;

import org.enodeframework.eventing.DomainEvent;

public class PaymentRejected extends DomainEvent<String> {
    public String orderId;
    public String conferenceId;

    public PaymentRejected() {
    }

    public PaymentRejected(String orderId, String conferenceId) {
        this.orderId = orderId;
        this.conferenceId = conferenceId;
    }
}
