package com.microsoft.conference.registration.domain.order.events;

import com.microsoft.conference.registration.domain.order.models.OrderStatus;

public class OrderPaymentConfirmed extends OrderEvent {
    public OrderStatus orderStatus;

    public OrderPaymentConfirmed() {
    }

    public OrderPaymentConfirmed(String conferenceId, OrderStatus orderStatus) {
        super(conferenceId);
        this.orderStatus = orderStatus;
    }
}
