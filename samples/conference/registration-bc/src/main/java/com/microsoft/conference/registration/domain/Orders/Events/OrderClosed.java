package com.microsoft.conference.registration.domain.Orders.Events;


public class OrderClosed extends OrderEvent {
    public OrderClosed() {
    }

    public OrderClosed(String conferenceId) {
        super(conferenceId);
    }
}

