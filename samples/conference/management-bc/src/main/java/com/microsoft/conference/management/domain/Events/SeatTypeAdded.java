package com.microsoft.conference.management.domain.Events;

import com.microsoft.conference.management.domain.Models.SeatTypeInfo;

public class SeatTypeAdded extends SeatTypeEvent {
    public int Quantity;

    public SeatTypeAdded() {
    }

    public SeatTypeAdded(String seatTypeId, SeatTypeInfo seatTypeInfo, int quantity) {
        super(seatTypeId, seatTypeInfo);
        Quantity = quantity;
    }
}
