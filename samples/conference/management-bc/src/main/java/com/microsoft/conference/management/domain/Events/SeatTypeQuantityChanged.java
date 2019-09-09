package com.microsoft.conference.management.domain.Events;

import org.enodeframework.eventing.DomainEvent;

public class SeatTypeQuantityChanged extends DomainEvent<String> {
    public String SeatTypeId;
    public int Quantity;
    public int AvailableQuantity;

    public SeatTypeQuantityChanged() {
    }

    public SeatTypeQuantityChanged(String seatTypeId, int quantity, int availableQuantity) {
        SeatTypeId = seatTypeId;
        Quantity = quantity;
        AvailableQuantity = availableQuantity;
    }
}
