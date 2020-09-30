package com.microsoft.conference.management.domain.models;

public class SeatQuantity {
    public String seatTypeId;
    public int quantity;

    public SeatQuantity(String seatTypeId, int quantity) {
        this.seatTypeId = seatTypeId;
        this.quantity = quantity;
    }
}
