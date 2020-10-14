package com.microsoft.conference.registration.domain.orders.events;

public class OrderClosed extends OrderEvent {
    public OrderClosed() {
    }

    public OrderClosed(String conferenceId) {
        super(conferenceId);
    }
}
