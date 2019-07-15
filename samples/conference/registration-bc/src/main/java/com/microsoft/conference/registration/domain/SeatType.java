package com.microsoft.conference.registration.domain;

import java.math.BigDecimal;

public class SeatType {
    public String SeatTypeId;
    public String SeatTypeName;
    public BigDecimal UnitPrice;

    public SeatType() {
    }

    public SeatType(String seatTypeId, String seatTypeName, BigDecimal unitPrice) {
        SeatTypeId = seatTypeId;
        SeatTypeName = seatTypeName;
        UnitPrice = unitPrice;

    }
}
