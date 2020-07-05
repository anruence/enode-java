package com.microsoft.conference.management.domain.models;

public class SeatAvailableQuantity {
    public String SeatTypeId;
    public int AvailableQuantity;

    public SeatAvailableQuantity(String seatTypeId, int availableQuantity) {
        SeatTypeId = seatTypeId;
        AvailableQuantity = availableQuantity;
    }
}
