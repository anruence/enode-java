package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.SeatTypeInfo;

public class SeatTypeAdded extends SeatTypeEvent {
    public int Quantity;

    public SeatTypeAdded() {
    }

    public SeatTypeAdded(String seatTypeId, SeatTypeInfo seatTypeInfo, int quantity) {
        super(seatTypeId, seatTypeInfo);
        Quantity = quantity;
    }
}
