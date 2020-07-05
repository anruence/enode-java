package com.microsoft.conference.registration.domain.seatassigning.Events;

import com.microsoft.conference.registration.domain.seatassigning.Models.Attendee;
import com.microsoft.conference.registration.domain.SeatType;
import org.enodeframework.eventing.DomainEvent;

public class SeatAssigned extends DomainEvent<String> {
    public int Position;
    public SeatType Seat;
    public Attendee attendee;

    public SeatAssigned() {
    }

    public SeatAssigned(int position, SeatType seat, Attendee attendee) {
        Position = position;
        Seat = seat;
        this.attendee = attendee;
    }
}
