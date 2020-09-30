package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.SeatTypeInfo;
import org.enodeframework.eventing.DomainEvent;

public abstract class SeatTypeEvent extends DomainEvent<String> {
    public String seatTypeId;
    public SeatTypeInfo seatTypeInfo;

    public SeatTypeEvent() {
    }

    public SeatTypeEvent(String seatTypeId, SeatTypeInfo seatTypeInfo) {
        this.seatTypeId = seatTypeId;
        this.seatTypeInfo = seatTypeInfo;
    }
}
