package com.microsoft.conference.management.domain.events;

import org.enodeframework.eventing.DomainEvent;

public class SeatTypeQuantityChanged extends DomainEvent<String> {
    public String seatTypeId;
    public int quantity;
    public int availableQuantity;

    public SeatTypeQuantityChanged() {
    }

    public SeatTypeQuantityChanged(String seatTypeId, int quantity, int availableQuantity) {
        this.seatTypeId = seatTypeId;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
    }
}
