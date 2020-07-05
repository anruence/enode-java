package com.microsoft.conference.registration.domain.orders.Events;

import com.microsoft.conference.registration.domain.orders.Models.OrderStatus;

public class OrderReservationConfirmed extends OrderEvent {
    public OrderStatus orderStatus;

    public OrderReservationConfirmed() {
    }

    public OrderReservationConfirmed(String conferenceId, OrderStatus orderStatus) {
        super(conferenceId);
        this.orderStatus = orderStatus;
    }
}
