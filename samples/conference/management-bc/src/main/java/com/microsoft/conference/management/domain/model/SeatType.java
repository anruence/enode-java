package com.microsoft.conference.management.domain.model;

public class SeatType {
    private String id;
    private SeatTypeInfo seatTypeInfo;
    private int quantity;

    public SeatType() {
    }

    public SeatType(String id, SeatTypeInfo seatTypeInfo) {
        this.id = id;
        this.seatTypeInfo = seatTypeInfo;
    }

    public String getId() {
        return this.id;
    }

    public SeatTypeInfo getSeatTypeInfo() {
        return this.seatTypeInfo;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSeatTypeInfo(SeatTypeInfo seatTypeInfo) {
        this.seatTypeInfo = seatTypeInfo;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
