package com.microsoft.conference.registration.domain.orders.events;

import com.microsoft.conference.registration.domain.orders.models.OrderStatus;

public class OrderReservationConfirmed extends OrderEvent {
    public OrderStatus orderStatus;

    public OrderReservationConfirmed() {
    }

    public OrderReservationConfirmed(String conferenceId, OrderStatus orderStatus) {
        super(conferenceId);
        this.orderStatus = orderStatus;
    }
}
