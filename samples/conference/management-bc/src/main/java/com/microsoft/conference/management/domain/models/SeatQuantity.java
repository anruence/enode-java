package com.microsoft.conference.management.domain.models;

public class SeatQuantity {
    private String seatTypeId;
    private int quantity;

    public SeatQuantity(String seatTypeId, int quantity) {
        this.seatTypeId = seatTypeId;
        this.quantity = quantity;
    }

    public String getSeatTypeId() {
        return this.seatTypeId;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setSeatTypeId(String seatTypeId) {
        this.seatTypeId = seatTypeId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
