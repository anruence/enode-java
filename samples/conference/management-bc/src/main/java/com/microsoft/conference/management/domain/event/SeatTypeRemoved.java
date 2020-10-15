package com.microsoft.conference.management.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

@Getter
@Setter
public class SeatTypeRemoved extends DomainEvent<String> {
    private String seatTypeId;

    public SeatTypeRemoved() {
    }

    public SeatTypeRemoved(String seatTypeId) {
        this.seatTypeId = seatTypeId;
    }
}
