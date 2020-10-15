package com.microsoft.conference.registration.domain.seatassigning.event;

import com.microsoft.conference.registration.domain.SeatType;
import com.microsoft.conference.registration.domain.seatassigning.model.Attendee;
import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

@Getter
@Setter
public class SeatAssigned extends DomainEvent<String> {
    private int position;
    private SeatType seatType;
    private Attendee attendee;

    public SeatAssigned() {
    }

    public SeatAssigned(int position, SeatType seatType, Attendee attendee) {
        this.position = position;
        this.seatType = seatType;
        this.attendee = attendee;
    }
}
