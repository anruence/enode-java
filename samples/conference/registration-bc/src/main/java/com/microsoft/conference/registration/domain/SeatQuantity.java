package com.microsoft.conference.registration.domain;

public class SeatQuantity {
    public SeatType seatType;
    public int quantity;

    public SeatQuantity() {
    }

    public SeatQuantity(SeatType seatType, int quantity) {
        this.seatType = seatType;
        this.quantity = quantity;
    }
}
