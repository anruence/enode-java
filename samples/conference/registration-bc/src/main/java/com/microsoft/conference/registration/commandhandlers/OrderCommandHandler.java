package com.microsoft.conference.registration.commandhandlers;

import com.enodeframework.commanding.ICommandContext;
import com.microsoft.conference.common.registration.commands.Orders.AssignRegistrantDetails;
import com.microsoft.conference.common.registration.commands.Orders.CloseOrder;
import com.microsoft.conference.common.registration.commands.Orders.ConfirmPayment;
import com.microsoft.conference.common.registration.commands.Orders.ConfirmReservation;
import com.microsoft.conference.common.registration.commands.Orders.MarkAsSuccess;
import com.microsoft.conference.common.registration.commands.Orders.PlaceOrder;
import com.microsoft.conference.registration.domain.Orders.IPricingService;
import com.microsoft.conference.registration.domain.Orders.Models.Order;
import com.microsoft.conference.registration.domain.SeatQuantity;
import com.microsoft.conference.registration.domain.SeatType;

import java.util.ArrayList;
import java.util.List;

import static com.enodeframework.common.io.Task.await;

public class OrderCommandHandler {
    private IPricingService _pricingService;

    public OrderCommandHandler(IPricingService pricingService) {
        _pricingService = pricingService;
    }

    //  .stream().filter(x -> new SeatQuantity(new SeatType(x.SeatType, x.SeatName, x.UnitPrice), x.Quantity)).findFirst().orElse(null),
    public void HandleAsync(ICommandContext context, PlaceOrder command) {
        List<SeatQuantity> seats = new ArrayList<>();
        command.Seats.forEach(x -> seats.add(new SeatQuantity(new SeatType(x.SeatType, x.SeatName, x.UnitPrice), x.Quantity)));
        context.addAsync(new Order(
                command.aggregateRootId,
                command.ConferenceId,
                seats,
                _pricingService));
    }

    public void HandleAsync(ICommandContext context, AssignRegistrantDetails command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.AssignRegistrant(command.FirstName, command.LastName, command.Email);
    }

    public void HandleAsync(ICommandContext context, ConfirmReservation command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.ConfirmReservation(command.IsReservationSuccess);
    }

    public void HandleAsync(ICommandContext context, ConfirmPayment command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.ConfirmPayment(command.IsPaymentSuccess);
    }

    public void HandleAsync(ICommandContext context, MarkAsSuccess command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.MarkAsSuccess();
    }

    public void HandleAsync(ICommandContext context, CloseOrder command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.Close();
    }
}
