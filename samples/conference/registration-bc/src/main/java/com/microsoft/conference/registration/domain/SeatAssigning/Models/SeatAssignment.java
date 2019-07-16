package com.microsoft.conference.registration.domain.SeatAssigning.Models;

import com.microsoft.conference.registration.domain.SeatType;

public class SeatAssignment {
    public int Position;
    public SeatType Seat;
    public Attendee attendee;

    public SeatAssignment() {
    }

    public SeatAssignment(int position, SeatType seat) {
        Position = position;
        Seat = seat;
    }

}
