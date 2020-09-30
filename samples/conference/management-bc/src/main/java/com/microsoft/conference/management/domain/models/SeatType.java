package com.microsoft.conference.management.domain.models;

public class SeatType {
    public String id;
    public SeatTypeInfo seatTypeInfo;
    public int quantity;

    public SeatType(String id, SeatTypeInfo seatTypeInfo) {
        this.id = id;
        this.seatTypeInfo = seatTypeInfo;
    }
}
