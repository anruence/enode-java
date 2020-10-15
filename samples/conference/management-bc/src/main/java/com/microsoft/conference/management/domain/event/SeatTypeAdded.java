package com.microsoft.conference.management.domain.event;

import com.microsoft.conference.management.domain.model.SeatTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatTypeAdded extends SeatTypeEvent {
    private int quantity;

    public SeatTypeAdded() {
    }

    public SeatTypeAdded(String seatTypeId, SeatTypeInfo seatTypeInfo, int quantity) {
        super(seatTypeId, seatTypeInfo);
        this.quantity = quantity;
    }
}
