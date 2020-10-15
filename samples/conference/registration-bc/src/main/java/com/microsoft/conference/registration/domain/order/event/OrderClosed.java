package com.microsoft.conference.registration.domain.order.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderClosed extends OrderEvent {
    public OrderClosed() {
    }

    public OrderClosed(String conferenceId) {
        super(conferenceId);
    }
}
