package com.microsoft.conference.registration.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SeatType {
    private String seatTypeId;
    private String seatTypeName;
    private BigDecimal unitPrice;

    public SeatType() {
    }

    public SeatType(String seatTypeId, String seatTypeName, BigDecimal unitPrice) {
        this.seatTypeId = seatTypeId;
        this.seatTypeName = seatTypeName;
        this.unitPrice = unitPrice;
    }
}
