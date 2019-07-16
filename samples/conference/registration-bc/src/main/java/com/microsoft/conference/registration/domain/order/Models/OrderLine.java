package com.microsoft.conference.registration.domain.order.models;

import com.microsoft.conference.registration.domain.SeatQuantity;

import java.math.BigDecimal;

public class OrderLine {
    public SeatQuantity seatQuantity;
    public BigDecimal lineTotal;

    public OrderLine() {
    }

    public OrderLine(SeatQuantity seatQuantity, BigDecimal lineTotal) {
        this.seatQuantity = seatQuantity;
        this.lineTotal = lineTotal;
    }
}
