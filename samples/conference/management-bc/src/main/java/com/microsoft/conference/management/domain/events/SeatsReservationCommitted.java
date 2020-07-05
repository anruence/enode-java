package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.SeatQuantity;
import org.enodeframework.eventing.DomainEvent;

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