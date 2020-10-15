package com.microsoft.conference.registration.domain.order.model;

import com.microsoft.conference.registration.domain.SeatQuantity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderLine {
    private SeatQuantity seatQuantity;

    private BigDecimal lineTotal;

    public OrderLine() {
    }

    public OrderLine(SeatQuantity seatQuantity, BigDecimal lineTotal) {
        this.seatQuantity = seatQuantity;
        this.lineTotal = lineTotal;
    }
}
