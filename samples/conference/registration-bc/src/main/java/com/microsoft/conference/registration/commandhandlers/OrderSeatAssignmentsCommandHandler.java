package com.microsoft.conference.registration.commandhandlers;

import com.microsoft.conference.common.registration.commands.seatassignments.AssignSeat;
import com.microsoft.conference.common.registration.commands.seatassignments.CreateSeatAssignments;
import com.microsoft.conference.common.registration.commands.seatassignments.UnassignSeat;
import com.microsoft.conference.registration.domain.orders.models.Order;
import com.microsoft.conference.registration.domain.seatassigning.Models.Attendee;
import com.microsoft.conference.registration.domain.seatassigning.Models.OrderSeatAssignments;
import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;

import static org.enodeframework.common.io.Task.await;

@Command
public class OrderSeatAssignmentsCommandHandler {

    @Subscribe
    public void handleAsync(ICommandContext context, CreateSeatAssignments command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        OrderSeatAssignments orderSeatAssignments = order.createSeatAssignments();
        context.add(orderSeatAssignments);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, AssignSeat command) {
        OrderSeatAssignments orderSeatAssignments = await(context.getAsync(command.aggregateRootId, OrderSeatAssignments.class));
        orderSeatAssignments.assignSeat(command.getPosition(), new Attendee(
                command.getPersonalInfo().getFirstName(),
                command.getPersonalInfo().getLastName(),
                command.getPersonalInfo().getEmail()));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, UnassignSeat command) {
        OrderSeatAssignments orderSeatAssignments = await(context.getAsync(command.aggregateRootId, OrderSeatAssignments.class));
        orderSeatAssignments.unAssignSeat(command.position);
    }
}
