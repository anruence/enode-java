package com.microsoft.conference.registration.domain.seatassigning.Events;

import com.microsoft.conference.registration.domain.seatassigning.Models.SeatAssignment;
import org.enodeframework.eventing.DomainEvent;

import java.util.List;

public class OrderSeatAssignmentsCreated extends DomainEvent<String> {
    public String orderId;
    public List<SeatAssignment> seatAssignments;

    public OrderSeatAssignmentsCreated() {
    }

    public OrderSeatAssignmentsCreated(String orderId, List<SeatAssignment> seatAssignments) {
        this.orderId = orderId;
        this.seatAssignments = seatAssignments;
    }
}
