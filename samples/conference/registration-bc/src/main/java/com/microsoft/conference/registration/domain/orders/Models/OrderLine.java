package com.microsoft.conference.registration.domain.orders.Models;

import java.math.BigDecimal;

public class OrderLine {
    public com.microsoft.conference.registration.domain.SeatQuantity SeatQuantity;
    public BigDecimal LineTotal;

    public OrderLine() {
    }

    public OrderLine(com.microsoft.conference.registration.domain.SeatQuantity seatQuantity, BigDecimal lineTotal) {
        SeatQuantity = seatQuantity;
        LineTotal = lineTotal;
    }
}
