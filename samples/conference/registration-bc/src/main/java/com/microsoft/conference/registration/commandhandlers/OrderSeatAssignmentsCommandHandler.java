package com.microsoft.conference.registration.commandhandlers;

import com.microsoft.conference.common.registration.commands.seatassignments.AssignSeat;
import com.microsoft.conference.common.registration.commands.seatassignments.CreateSeatAssignments;
import com.microsoft.conference.common.registration.commands.seatassignments.UnassignSeat;
import com.microsoft.conference.registration.domain.orders.Models.Order;
import com.microsoft.conference.registration.domain.seatassigning.Models.Attendee;
import com.microsoft.conference.registration.domain.seatassigning.Models.OrderSeatAssignments;
import org.enodeframework.commanding.ICommandContext;

import static org.enodeframework.common.io.Task.await;

public class OrderSeatAssignmentsCommandHandler {
    public void HandleAsync(ICommandContext context, CreateSeatAssignments command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        OrderSeatAssignments orderSeatAssignments = order.createSeatAssignments();
        context.add(orderSeatAssignments);
    }

    public void HandleAsync(ICommandContext context, AssignSeat command) {
        OrderSeatAssignments orderSeatAssignments = await(context.getAsync(command.aggregateRootId, OrderSeatAssignments.class));
        orderSeatAssignments.assignSeat(command.position, new Attendee(
                command.personalInfo.firstName,
                command.personalInfo.lastName,
                command.personalInfo.email));
    }

    public void HandleAsync(ICommandContext context, UnassignSeat command) {
        OrderSeatAssignments orderSeatAssignments = await(context.getAsync(command.aggregateRootId, OrderSeatAssignments.class));
        orderSeatAssignments.unAssignSeat(command.position);
    }
}
