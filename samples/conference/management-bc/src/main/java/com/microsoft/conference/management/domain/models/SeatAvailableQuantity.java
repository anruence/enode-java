package com.microsoft.conference.management.domain.models;

public class SeatAvailableQuantity {
    private String seatTypeId;
    private int availableQuantity;

    public SeatAvailableQuantity(String seatTypeId, int availableQuantity) {
        this.seatTypeId = seatTypeId;
        this.availableQuantity = availableQuantity;
    }

    public String getSeatTypeId() {
        return this.seatTypeId;
    }

    public int getAvailableQuantity() {
        return this.availableQuantity;
    }

    public void setSeatTypeId(String seatTypeId) {
        this.seatTypeId = seatTypeId;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
