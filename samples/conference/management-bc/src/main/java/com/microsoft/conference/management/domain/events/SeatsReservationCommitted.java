package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.SeatQuantity;
import org.enodeframework.eventing.DomainEvent;

import java.util.List;

public class SeatsReservationCommitted extends DomainEvent<String> {
    private String reservationId;
    private List<SeatQuantity> seatQuantities;

    public SeatsReservationCommitted() {
    }

    public SeatsReservationCommitted(String reservationId, List<SeatQuantity> seatQuantities) {
        this.reservationId = reservationId;
        this.seatQuantities = seatQuantities;
    }

    public String getReservationId() {
        return this.reservationId;
    }

    public List<SeatQuantity> getSeatQuantities() {
        return this.seatQuantities;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public void setSeatQuantities(List<SeatQuantity> seatQuantities) {
        this.seatQuantities = seatQuantities;
    }
}