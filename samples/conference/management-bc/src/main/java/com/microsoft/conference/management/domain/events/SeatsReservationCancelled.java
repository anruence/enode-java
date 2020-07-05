package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.SeatAvailableQuantity;
import org.enodeframework.eventing.DomainEvent;

import java.util.List;

public class SeatsReservationCancelled extends DomainEvent<String> {
    public String ReservationId;
    public List<SeatAvailableQuantity> SeatAvailableQuantities;

    public SeatsReservationCancelled() {
    }

    public SeatsReservationCancelled(String reservationId, List<SeatAvailableQuantity> seatAvailableQuantities) {
        ReservationId = reservationId;
        SeatAvailableQuantities = seatAvailableQuantities;
    }
}
