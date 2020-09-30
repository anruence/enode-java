package com.microsoft.conference.registration.domain.seatassigning.Events;

import org.enodeframework.eventing.DomainEvent;

public class SeatUnassigned extends DomainEvent<String> {
    public int position;

    public SeatUnassigned() {
    }

    public SeatUnassigned(int position) {
        this.position = position;
    }
}
