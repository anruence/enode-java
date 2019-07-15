package com.microsoft.conference.payments.domain.Events;

import com.enodeframework.eventing.DomainEvent;

public class PaymentRejected extends DomainEvent<String> {
    public String OrderId;
    public String ConferenceId;

    public PaymentRejected() {
    }

    public PaymentRejected(String orderId, String conferenceId) {
        OrderId = orderId;
        ConferenceId = conferenceId;
    }

}
