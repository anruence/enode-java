package com.microsoft.conference.registration.commandhandlers;

import com.microsoft.conference.common.registration.commands.SeatAssignments.AssignSeat;
import com.microsoft.conference.common.registration.commands.SeatAssignments.CreateSeatAssignments;
import com.microsoft.conference.common.registration.commands.SeatAssignments.UnassignSeat;
import com.microsoft.conference.registration.domain.Orders.Models.Order;
import com.microsoft.conference.registration.domain.SeatAssigning.Models.Attendee;
import com.microsoft.conference.registration.domain.SeatAssigning.Models.OrderSeatAssignments;
import org.enodeframework.commanding.ICommandContext;

import static org.enodeframework.common.io.Task.await;

public class OrderSeatAssignmentsCommandHandler {
    public void HandleAsync(ICommandContext context, CreateSeatAssignments command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        OrderSeatAssignments orderSeatAssignments = order.CreateSeatAssignments();
        context.add(orderSeatAssignments);
    }

    public void HandleAsync(ICommandContext context, AssignSeat command) {
        OrderSeatAssignments orderSeatAssignments = await(context.getAsync(command.aggregateRootId, OrderSeatAssignments.class));
        orderSeatAssignments.AssignSeat(command.Position, new Attendee(
                command.PersonalInfo.FirstName,
                command.PersonalInfo.LastName,
                command.PersonalInfo.Email));
    }

    public void HandleAsync(ICommandContext context, UnassignSeat command) {
        OrderSeatAssignments orderSeatAssignments = await(context.getAsync(command.aggregateRootId, OrderSeatAssignments.class));
        orderSeatAssignments.UnassignSeat(command.Position);
    }
}
