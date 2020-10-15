package com.microsoft.conference.registration.domain.order.events;

public class OrderClosed extends OrderEvent {
    public OrderClosed() {
    }

    public OrderClosed(String conferenceId) {
        super(conferenceId);
    }
}
