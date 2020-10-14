package com.microsoft.conference.management.domain.events;

import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

@Getter
@Setter
public class SeatTypeQuantityChanged extends DomainEvent<String> {
    private String seatTypeId;
    private int quantity;
    private int availableQuantity;

    public SeatTypeQuantityChanged() {
    }

    public SeatTypeQuantityChanged(String seatTypeId, int quantity, int availableQuantity) {
        this.seatTypeId = seatTypeId;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
    }
}
