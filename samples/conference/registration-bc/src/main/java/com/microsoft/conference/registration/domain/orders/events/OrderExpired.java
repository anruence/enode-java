package com.microsoft.conference.registration.domain.orders.events;

public class OrderExpired extends OrderEvent {
    public OrderExpired() {
    }

    public OrderExpired(String conferenceId) {
        super(conferenceId);
    }
}
