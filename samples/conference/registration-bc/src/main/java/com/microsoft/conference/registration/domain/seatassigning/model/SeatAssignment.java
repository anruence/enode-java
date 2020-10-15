package com.microsoft.conference.registration.domain.seatassigning.model;

import com.microsoft.conference.registration.domain.SeatType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatAssignment {
    private int position;
    private SeatType seatType;
    private Attendee attendee;

    public SeatAssignment() {
    }

    public SeatAssignment(int position, SeatType seatType) {
        this.position = position;
        this.seatType = seatType;
    }
}
