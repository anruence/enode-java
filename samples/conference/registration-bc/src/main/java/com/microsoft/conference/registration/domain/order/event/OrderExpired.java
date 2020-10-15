package com.microsoft.conference.registration.domain.order.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderExpired extends OrderEvent {
    public OrderExpired() {
    }

    public OrderExpired(String conferenceId) {
        super(conferenceId);
    }
}
