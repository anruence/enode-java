package com.microsoft.conference.registration.domain.order.events;

public class OrderSuccessed extends OrderEvent {
    public OrderSuccessed() {
    }

    public OrderSuccessed(String conferenceId) {
        super(conferenceId);
    }
}
