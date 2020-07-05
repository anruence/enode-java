package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.SeatTypeInfo;

public class SeatTypeUpdated extends SeatTypeEvent {
    public SeatTypeUpdated() {
    }

    public SeatTypeUpdated(String seatTypeId, SeatTypeInfo seatTypeInfo) {
        super(seatTypeId, seatTypeInfo);
    }
}
