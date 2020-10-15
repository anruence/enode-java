package com.microsoft.conference.registration.domain.seatassigning.model;

import com.microsoft.conference.common.Linq;
import com.microsoft.conference.common.exception.ArgumentOutOfRangeException;
import com.microsoft.conference.registration.domain.order.model.OrderLine;
import com.microsoft.conference.registration.domain.seatassigning.event.OrderSeatAssignmentsCreated;
import com.microsoft.conference.registration.domain.seatassigning.event.SeatAssigned;
import com.microsoft.conference.registration.domain.seatassigning.event.SeatUnassigned;
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
            for (int i = 0; i < orderLine.getSeatQuantity().getQuantity(); i++) {
                assignments.add(new SeatAssignment(position++, orderLine.getSeatQuantity().getSeatType()));
            }
        }
        applyEvent(new OrderSeatAssignmentsCreated(orderId, assignments));
    }

    public void assignSeat(int position, Attendee attendee) {
        SeatAssignment current = Linq.singleOrDefault(seatAssignments, x -> x.getPosition() == position);
        if (current == null) {
            throw new ArgumentOutOfRangeException("position");
        }
        if (current.getAttendee() == null || attendee != current.getAttendee()) {
            applyEvent(new SeatAssigned(current.getPosition(), current.getSeatType(), attendee));
        }
    }

    public void unAssignSeat(int position) {
        SeatAssignment current = Linq.singleOrDefault(seatAssignments, x -> x.getPosition() == position);
        if (current == null) {
            throw new ArgumentOutOfRangeException("position");
        }
        applyEvent(new SeatUnassigned(position));
    }

    private void handle(OrderSeatAssignmentsCreated evnt) {
        id = evnt.getAggregateRootId();
        orderId = evnt.getOrderId();
        seatAssignments = evnt.getSeatAssignments();
    }

    private void handle(SeatAssigned evnt) {
        Linq.single(seatAssignments, x -> x.getPosition() == evnt.getPosition()).setAttendee(evnt.getAttendee());
    }

    private void handle(SeatUnassigned evnt) {
        Linq.single(seatAssignments, x -> x.getPosition() == evnt.getPosition()).setAttendee(null);
    }
}
