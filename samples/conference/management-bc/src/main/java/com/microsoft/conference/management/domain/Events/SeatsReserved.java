package com.microsoft.conference.management.domain.Events;

import com.microsoft.conference.management.domain.Models.ReservationItem;
import com.microsoft.conference.management.domain.Models.SeatAvailableQuantity;
import org.enodeframework.eventing.DomainEvent;

import java.util.List;

public class SeatsReserved extends DomainEvent<String> {
    public String ReservationId;
    public List<ReservationItem> ReservationItems;
    public List<SeatAvailableQuantity> SeatAvailableQuantities;

    public SeatsReserved() {
    }

    public SeatsReserved(String reservationId, List<ReservationItem> reservationItems, List<SeatAvailableQuantity> seatAvailableQuantities) {
        ReservationId = reservationId;
        ReservationItems = reservationItems;
        SeatAvailableQuantities = seatAvailableQuantities;
    }
}