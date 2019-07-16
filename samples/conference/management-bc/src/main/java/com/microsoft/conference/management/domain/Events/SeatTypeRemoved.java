package com.microsoft.conference.management.domain.Events;

import com.enodeframework.eventing.DomainEvent;

public class SeatTypeRemoved extends DomainEvent<String> {
    public String SeatTypeId;

    public SeatTypeRemoved() {
    }

    public SeatTypeRemoved(String seatTypeId) {
        SeatTypeId = seatTypeId;
    }
}
