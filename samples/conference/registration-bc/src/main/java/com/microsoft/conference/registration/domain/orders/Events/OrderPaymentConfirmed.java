package com.microsoft.conference.registration.domain.orders.Events;

import com.microsoft.conference.registration.domain.orders.Models.OrderStatus;

public class OrderPaymentConfirmed extends OrderEvent {
    public OrderStatus orderStatus;

    public OrderPaymentConfirmed() {
    }

    public OrderPaymentConfirmed(String conferenceId, OrderStatus orderStatus) {
        super(conferenceId);
        this.orderStatus = orderStatus;
    }
}
