package com.microsoft.conference.management.domain.Events;

import com.microsoft.conference.management.domain.Models.SeatTypeInfo;
import org.enodeframework.eventing.DomainEvent;

public abstract class SeatTypeEvent extends DomainEvent<String> {
    public String SeatTypeId;
    public SeatTypeInfo SeatTypeInfo;

    public SeatTypeEvent() {
    }

    public SeatTypeEvent(String seatTypeId, SeatTypeInfo seatTypeInfo) {
        SeatTypeId = seatTypeId;
        SeatTypeInfo = seatTypeInfo;
    }
}
