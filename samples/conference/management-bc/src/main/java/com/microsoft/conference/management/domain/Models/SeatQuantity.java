package com.microsoft.conference.management.domain.Models;

public class SeatQuantity {
    public String SeatTypeId;
    public int Quantity;

    public SeatQuantity(String seatTypeId, int quantity) {
        SeatTypeId = seatTypeId;
        Quantity = quantity;
    }
}
