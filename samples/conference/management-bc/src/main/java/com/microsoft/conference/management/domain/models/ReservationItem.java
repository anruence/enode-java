package com.microsoft.conference.management.domain.models;

public class ReservationItem {
    public String seatTypeId;
    public int quantity;

    public ReservationItem(String seatTypeId, int quantity) {
        this.seatTypeId = seatTypeId;
        this.quantity = quantity;
    }
}
