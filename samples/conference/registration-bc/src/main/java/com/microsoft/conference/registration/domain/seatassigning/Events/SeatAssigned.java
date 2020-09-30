package com.microsoft.conference.registration.domain.seatassigning.Events;

import com.microsoft.conference.registration.domain.SeatType;
import com.microsoft.conference.registration.domain.seatassigning.Models.Attendee;
import org.enodeframework.eventing.DomainEvent;

public class SeatAssigned extends DomainEvent<String> {
    public int position;
    public SeatType seatType;
    public Attendee attendee;

    public SeatAssigned() {
    }

    public SeatAssigned(int position, SeatType seatType, Attendee attendee) {
        this.position = position;
        this.seatType = seatType;
        this.attendee = attendee;
    }
}
