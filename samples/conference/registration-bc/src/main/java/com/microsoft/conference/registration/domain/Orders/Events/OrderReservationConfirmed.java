package com.microsoft.conference.registration.domain.Orders.Events;


import com.microsoft.conference.registration.domain.Orders.Models.OrderStatus;

public class OrderReservationConfirmed extends OrderEvent {
    public OrderStatus orderStatus;

    public OrderReservationConfirmed() {
    }

    public OrderReservationConfirmed(String conferenceId, OrderStatus orderStatus) {
        super(conferenceId);
        this.orderStatus = orderStatus;
    }
}
