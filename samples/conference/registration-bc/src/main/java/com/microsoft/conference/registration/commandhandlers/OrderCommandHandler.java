package com.microsoft.conference.registration.commandhandlers;

import com.microsoft.conference.common.registration.commands.orders.AssignRegistrantDetails;
import com.microsoft.conference.common.registration.commands.orders.CloseOrder;
import com.microsoft.conference.common.registration.commands.orders.ConfirmPayment;
import com.microsoft.conference.common.registration.commands.orders.ConfirmReservation;
import com.microsoft.conference.common.registration.commands.orders.MarkAsSuccess;
import com.microsoft.conference.common.registration.commands.orders.PlaceOrder;
import com.microsoft.conference.registration.domain.SeatQuantity;
import com.microsoft.conference.registration.domain.SeatType;
import com.microsoft.conference.registration.domain.orders.IPricingService;
import com.microsoft.conference.registration.domain.orders.Models.Order;
import org.enodeframework.commanding.ICommandContext;

import java.util.ArrayList;
import java.util.List;

import static org.enodeframework.common.io.Task.await;

public class OrderCommandHandler {
    private IPricingService pricingService;

    public OrderCommandHandler(IPricingService pricingService) {
        this.pricingService = pricingService;
    }

    //  .stream().filter(x -> new SeatQuantity(new SeatType(x.SeatType, x.SeatName, x.UnitPrice), x.Quantity)).findFirst().orElse(null),
    public void HandleAsync(ICommandContext context, PlaceOrder command) {
        List<SeatQuantity> seats = new ArrayList<>();
        command.seatInfos.forEach(x -> seats.add(new SeatQuantity(new SeatType(x.seatType, x.seatName, x.unitPrice), x.quantity)));
        context.addAsync(new Order(
                command.aggregateRootId,
                command.conferenceId,
                seats,
                pricingService));
    }

    public void HandleAsync(ICommandContext context, AssignRegistrantDetails command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.assignRegistrant(command.firstName, command.lastName, command.email);
    }

    public void HandleAsync(ICommandContext context, ConfirmReservation command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.confirmReservation(command.isReservationSuccess);
    }

    public void HandleAsync(ICommandContext context, ConfirmPayment command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.confirmPayment(command.isPaymentSuccess);
    }

    public void HandleAsync(ICommandContext context, MarkAsSuccess command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.markAsSuccess();
    }

    public void HandleAsync(ICommandContext context, CloseOrder command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.close();
    }
}
