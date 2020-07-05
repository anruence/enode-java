package com.microsoft.conference.registration.domain.orders.Events;

public class OrderSuccessed extends OrderEvent {
    public OrderSuccessed() {
    }

    public OrderSuccessed(String conferenceId) {
        super(conferenceId);
    }
}
