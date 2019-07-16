package com.microsoft.conference.registration.commandhandlers;

import com.microsoft.conference.common.registration.commands.orders.AssignRegistrantDetails;
import com.microsoft.conference.common.registration.commands.orders.CloseOrder;
import com.microsoft.conference.common.registration.commands.orders.ConfirmPayment;
import com.microsoft.conference.common.registration.commands.orders.ConfirmReservation;
import com.microsoft.conference.common.registration.commands.orders.MarkAsSuccess;
import com.microsoft.conference.common.registration.commands.orders.PlaceOrder;
import com.microsoft.conference.registration.domain.SeatQuantity;
import com.microsoft.conference.registration.domain.SeatType;
import com.microsoft.conference.registration.domain.order.IPricingService;
import com.microsoft.conference.registration.domain.order.models.Order;
import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.enodeframework.common.io.Task.await;

@Command
public class OrderCommandHandler {

    @Autowired
    private IPricingService pricingService;

    @Subscribe
    public void handleAsync(ICommandContext context, PlaceOrder command) {
        List<SeatQuantity> seats = new ArrayList<>();
        command.seatInfos.forEach(x -> seats.add(new SeatQuantity(new SeatType(x.seatType, x.seatName, x.unitPrice), x.quantity)));
        context.addAsync(new Order(
                command.aggregateRootId,
                command.conferenceId,
                seats,
                pricingService));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, AssignRegistrantDetails command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.assignRegistrant(command.firstName, command.lastName, command.email);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmReservation command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.confirmReservation(command.isReservationSuccess);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, ConfirmPayment command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.confirmPayment(command.isPaymentSuccess);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, MarkAsSuccess command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.markAsSuccess();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CloseOrder command) {
        Order order = await(context.getAsync(command.aggregateRootId, Order.class));
        order.close();
    }
}
