package com.microsoft.conference.management.domain.Events;

import com.enodeframework.eventing.DomainEvent;
import com.microsoft.conference.management.domain.Models.SeatAvailableQuantity;

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
