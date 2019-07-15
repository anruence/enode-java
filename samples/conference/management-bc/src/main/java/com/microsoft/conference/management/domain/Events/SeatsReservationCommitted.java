package com.microsoft.conference.management.domain.Events;

import com.enodeframework.eventing.DomainEvent;
import com.microsoft.conference.management.domain.Models.SeatQuantity;

import java.util.List;

public class SeatsReservationCommitted extends DomainEvent<String> {
    public String ReservationId;
    public List<SeatQuantity> SeatQuantities;

    public SeatsReservationCommitted() {
    }

    public SeatsReservationCommitted(String reservationId, List<SeatQuantity> seatQuantities) {
        ReservationId = reservationId;
        SeatQuantities = seatQuantities;
    }
}