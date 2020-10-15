package com.microsoft.conference.registration.domain.seatassigning.event;

import com.microsoft.conference.registration.domain.seatassigning.model.SeatAssignment;
import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

import java.util.List;

@Getter
@Setter
public class OrderSeatAssignmentsCreated extends DomainEvent<String> {
    private String orderId;

    private List<SeatAssignment> seatAssignments;

    public OrderSeatAssignmentsCreated() {
    }

    public OrderSeatAssignmentsCreated(String orderId, List<SeatAssignment> seatAssignments) {
        this.orderId = orderId;
        this.seatAssignments = seatAssignments;
    }
}
