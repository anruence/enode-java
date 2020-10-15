package com.microsoft.conference.registration.domain.seatassigning.event;

import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

@Getter
@Setter
public class SeatUnassigned extends DomainEvent<String> {
    private int position;

    public SeatUnassigned() {
    }

    public SeatUnassigned(int position) {
        this.position = position;
    }
}
