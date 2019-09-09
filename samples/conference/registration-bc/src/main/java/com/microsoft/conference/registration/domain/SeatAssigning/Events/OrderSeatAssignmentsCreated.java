package com.microsoft.conference.registration.domain.SeatAssigning.Events;

import com.microsoft.conference.registration.domain.SeatAssigning.Models.SeatAssignment;
import org.enodeframework.eventing.DomainEvent;

import java.util.List;

public class OrderSeatAssignmentsCreated extends DomainEvent<String> {
    public String OrderId;
    public List<SeatAssignment> Assignments;

    public OrderSeatAssignmentsCreated() {
    }

    public OrderSeatAssignmentsCreated(String orderId, List<SeatAssignment> assignments) {
        OrderId = orderId;
        Assignments = assignments;
    }
}
