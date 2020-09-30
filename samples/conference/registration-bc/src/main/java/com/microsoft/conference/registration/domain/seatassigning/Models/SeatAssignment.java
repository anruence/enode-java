package com.microsoft.conference.registration.domain.seatassigning.Models;

import com.microsoft.conference.registration.domain.SeatType;

public class SeatAssignment {
    public int position;
    public SeatType seatType;
    public Attendee attendee;

    public SeatAssignment() {
    }

    public SeatAssignment(int position, SeatType seatType) {
        this.position = position;
        this.seatType = seatType;
    }
}
