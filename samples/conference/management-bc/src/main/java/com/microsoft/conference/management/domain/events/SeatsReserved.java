package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.ReservationItem;
import com.microsoft.conference.management.domain.models.SeatAvailableQuantity;
import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

import java.util.List;

@Getter
@Setter
public class SeatsReserved extends DomainEvent<String> {
    private String reservationId;
    private List<ReservationItem> reservationItems;
    private List<SeatAvailableQuantity> seatAvailableQuantities;

    public SeatsReserved() {
    }

    public SeatsReserved(String reservationId, List<ReservationItem> reservationItems, List<SeatAvailableQuantity> seatAvailableQuantities) {
        this.reservationId = reservationId;
        this.reservationItems = reservationItems;
        this.seatAvailableQuantities = seatAvailableQuantities;
    }
}