package com.microsoft.conference.payments.domain.events;

import com.microsoft.conference.payments.domain.models.Payment;
import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

@Getter
@Setter
public class PaymentCompleted extends DomainEvent<String> {
    private String orderId;
    private String conferenceId;

    public PaymentCompleted() {
    }

    public PaymentCompleted(Payment payment, String orderId, String conferenceId) {
        this.orderId = orderId;
        this.conferenceId = conferenceId;
    }
}
