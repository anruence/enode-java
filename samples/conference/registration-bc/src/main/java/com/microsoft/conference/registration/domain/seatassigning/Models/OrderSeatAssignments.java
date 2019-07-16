package com.microsoft.conference.registration.domain.seatassigning.Models;

import com.microsoft.conference.common.Linq;
import com.microsoft.conference.common.exception.ArgumentOutOfRangeException;
import com.microsoft.conference.registration.domain.order.models.OrderLine;
import com.microsoft.conference.registration.domain.seatassigning.Events.OrderSeatAssignmentsCreated;
import com.microsoft.conference.registration.domain.seatassigning.Events.SeatAssigned;
import com.microsoft.conference.registration.domain.seatassigning.Events.SeatUnassigned;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.domain.AggregateRoot;

import java.util.ArrayList;
import java.util.List;

public class OrderSeatAssignments extends AggregateRoot<String> {
    private String orderId;
    private List<SeatAssignment> seatAssignments;

    public OrderSeatAssignments(String orderId, List<OrderLine> orderLines) {
        super(ObjectId.generateNewStringId());
        Ensure.notNullOrEmpty(orderId, "orderId");
        Ensure.notNull(orderLines, "orderLines");
        if (orderLines.isEmpty()) {
            throw new IllegalArgumentException("The seats of order cannot be empty.");
        }
        int position = 0;
        List<SeatAssignment> assignments = new ArrayList<>();
        for (OrderLine orderLine : orderLines) {
            for (int i = 0; i < orderLine.seatQuantity.quantity; i++) {
                assignments.add(new SeatAssignment(position++, orderLine.seatQuantity.seatType));
            }
        }
        applyEvent(new OrderSeatAssignmentsCreated(orderId, assignments));
    }

    public void assignSeat(int position, Attendee attendee) {
        SeatAssignment current = Linq.singleOrDefault(seatAssignments, x -> x.position == position);
        if (current == null) {
            throw new ArgumentOutOfRangeException("position");
        }
        if (current.attendee == null || attendee != current.attendee) {
            applyEvent(new SeatAssigned(current.position, current.seatType, attendee));
        }
    }

    public void unAssignSeat(int position) {
        SeatAssignment current = Linq.singleOrDefault(seatAssignments, x -> x.position == position);
        if (current == null) {
            throw new ArgumentOutOfRangeException("position");
        }
        applyEvent(new SeatUnassigned(position));
    }

    private void handle(OrderSeatAssignmentsCreated evnt) {
        id = evnt.getAggregateRootId();
        orderId = evnt.orderId;
        seatAssignments = evnt.seatAssignments;
    }

    private void handle(SeatAssigned evnt) {
        Linq.single(seatAssignments, x -> x.position == evnt.position).attendee = evnt.attendee;
    }

    private void handle(SeatUnassigned evnt) {
        Linq.single(seatAssignments, x -> x.position == evnt.position).attendee = null;
    }
}
