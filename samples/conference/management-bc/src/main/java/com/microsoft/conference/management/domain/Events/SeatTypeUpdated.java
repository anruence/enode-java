package com.microsoft.conference.management.domain.Events;

import com.microsoft.conference.management.domain.Models.SeatTypeInfo;

public class SeatTypeUpdated extends SeatTypeEvent {
    public SeatTypeUpdated() {
    }

    public SeatTypeUpdated(String seatTypeId, SeatTypeInfo seatTypeInfo) {
        super(seatTypeId, seatTypeInfo);
    }
}
