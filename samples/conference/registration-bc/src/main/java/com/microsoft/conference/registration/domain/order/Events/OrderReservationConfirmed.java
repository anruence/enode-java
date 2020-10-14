package com.microsoft.conference.registration.domain.order.events;

import com.microsoft.conference.registration.domain.order.models.OrderStatus;

public class OrderReservationConfirmed extends OrderEvent {
    public OrderStatus orderStatus;

    public OrderReservationConfirmed() {
    }

    public OrderReservationConfirmed(String conferenceId, OrderStatus orderStatus) {
        super(conferenceId);
        this.orderStatus = orderStatus;
    }
}
