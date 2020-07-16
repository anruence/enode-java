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
