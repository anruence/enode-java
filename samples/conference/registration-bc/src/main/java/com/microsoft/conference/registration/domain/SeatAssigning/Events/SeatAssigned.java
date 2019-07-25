package com.microsoft.conference.registration.domain.SeatAssigning.Events;

import com.enodeframework.eventing.DomainEvent;
import com.microsoft.conference.registration.domain.SeatAssigning.Models.Attendee;
import com.microsoft.conference.registration.domain.SeatType;

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
