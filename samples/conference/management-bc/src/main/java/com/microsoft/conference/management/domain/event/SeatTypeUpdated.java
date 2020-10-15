package com.microsoft.conference.management.domain.event;

import com.microsoft.conference.management.domain.model.SeatTypeInfo;

public class SeatTypeUpdated extends SeatTypeEvent {
    public SeatTypeUpdated() {
    }

    public SeatTypeUpdated(String seatTypeId, SeatTypeInfo seatTypeInfo) {
        super(seatTypeId, seatTypeInfo);
    }
}
