package com.microsoft.conference.management.domain.models;

public class SeatAvailableQuantity {
    public String seatTypeId;
    public int availableQuantity;

    public SeatAvailableQuantity(String seatTypeId, int availableQuantity) {
        this.seatTypeId = seatTypeId;
        this.availableQuantity = availableQuantity;
    }
}
