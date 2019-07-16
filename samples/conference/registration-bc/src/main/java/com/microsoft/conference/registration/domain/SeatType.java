package com.microsoft.conference.registration.domain;

import java.math.BigDecimal;

public class SeatType {
    public String seatTypeId;
    public String seatTypeName;
    public BigDecimal unitPrice;

    public SeatType() {
    }

    public SeatType(String seatTypeId, String seatTypeName, BigDecimal unitPrice) {
        this.seatTypeId = seatTypeId;
        this.seatTypeName = seatTypeName;
        this.unitPrice = unitPrice;
    }
}
