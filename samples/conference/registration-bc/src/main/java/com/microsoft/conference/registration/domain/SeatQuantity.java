package com.microsoft.conference.registration.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatQuantity {
    private SeatType seatType;
    private int quantity;

    public SeatQuantity() {
    }

    public SeatQuantity(SeatType seatType, int quantity) {
        this.seatType = seatType;
        this.quantity = quantity;
    }
}
