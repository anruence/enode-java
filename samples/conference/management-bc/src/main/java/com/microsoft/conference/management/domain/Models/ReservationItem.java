package com.microsoft.conference.management.domain.Models;

public class ReservationItem {
    public String SeatTypeId;
    public int Quantity;
    public ReservationItem(String seatTypeId, int quantity) {
        SeatTypeId = seatTypeId;
        Quantity = quantity;
    }
}
