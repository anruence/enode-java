package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.SeatAvailableQuantity;
import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

import java.util.List;

@Getter
@Setter
public class SeatsReservationCancelled extends DomainEvent<String> {
    private String reservationId;
    private List<SeatAvailableQuantity> seatAvailableQuantities;

    public SeatsReservationCancelled() {
    }

    public SeatsReservationCancelled(String reservationId, List<SeatAvailableQuantity> seatAvailableQuantities) {
        this.reservationId = reservationId;
        this.seatAvailableQuantities = seatAvailableQuantities;
    }
}
