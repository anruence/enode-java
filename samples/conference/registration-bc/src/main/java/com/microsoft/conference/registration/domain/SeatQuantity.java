package com.microsoft.conference.registration.domain;

public class SeatQuantity {
    public SeatType Seat;
    public int Quantity;

    public SeatQuantity() {
    }

    public SeatQuantity(SeatType seat, int quantity) {
        Seat = seat;
        Quantity = quantity;
    }
}
