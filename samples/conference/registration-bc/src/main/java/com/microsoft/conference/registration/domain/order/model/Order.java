package com.microsoft.conference.registration.domain.order.model;

import com.microsoft.conference.common.exception.InvalidOperationException;
import com.microsoft.conference.registration.domain.SeatQuantity;
import com.microsoft.conference.registration.domain.order.PricingService;
import com.microsoft.conference.registration.domain.order.event.OrderClosed;
import com.microsoft.conference.registration.domain.order.event.OrderExpired;
import com.microsoft.conference.registration.domain.order.event.OrderPaymentConfirmed;
import com.microsoft.conference.registration.domain.order.event.OrderPlaced;
import com.microsoft.conference.registration.domain.order.event.OrderRegistrantAssigned;
import com.microsoft.conference.registration.domain.order.event.OrderReservationConfirmed;
import com.microsoft.conference.registration.domain.order.event.OrderSuccessed;
import com.microsoft.conference.registration.domain.seatassigning.model.OrderSeatAssignments;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.domain.AggregateRoot;

import java.util.Date;
import java.util.List;

public class Order extends AggregateRoot<String> {
    private OrderTotal total;
    private String conferenceId;
    private OrderStatus status;
    private Registrant registrant;
    private String accessCode;

    public Order(String id, String conferenceId, List<SeatQuantity> seats, PricingService pricingService) {
        super(id);
        Ensure.notNullOrEmpty(id, "id");
        Ensure.notNullOrEmpty(conferenceId, "conferenceId");
        Ensure.notNull(seats, "seats");
        Ensure.notNull(pricingService, "pricingService");
        if (seats.isEmpty()) {
            throw new IllegalArgumentException("The seats of order cannot be empty.");
        }
        OrderTotal orderTotal = pricingService.calculateTotal(conferenceId, seats);
        applyEvent(new OrderPlaced(conferenceId, orderTotal, new Date(), ObjectId.generateNewStringId()));
    }

    public void assignRegistrant(String firstName, String lastName, String email) {
        applyEvent(new OrderRegistrantAssigned(conferenceId, new Registrant(firstName, lastName, email)));
    }

    public void confirmReservation(boolean isReservationSuccess) {
        if (status != OrderStatus.Placed) {
            throw new InvalidOperationException("Invalid order status:" + status);
        }
        if (isReservationSuccess) {
            applyEvent(new OrderReservationConfirmed(conferenceId, OrderStatus.ReservationSuccess));
        } else {
            applyEvent(new OrderReservationConfirmed(conferenceId, OrderStatus.ReservationFailed));
        }
    }

    public void confirmPayment(boolean isPaymentSuccess) {
        if (status != OrderStatus.ReservationSuccess) {
            throw new InvalidOperationException("Invalid order status:" + status);
        }
        if (isPaymentSuccess) {
            applyEvent(new OrderPaymentConfirmed(conferenceId, OrderStatus.PaymentSuccess));
        } else {
            applyEvent(new OrderPaymentConfirmed(conferenceId, OrderStatus.PaymentRejected));
        }
    }

    public void markAsSuccess() {
        if (status != OrderStatus.PaymentSuccess) {
            throw new InvalidOperationException("Invalid order status:" + status);
        }
        applyEvent(new OrderSuccessed(conferenceId));
    }

    public void markAsExpire() {
        if (status == OrderStatus.ReservationSuccess) {
            applyEvent(new OrderExpired(conferenceId));
        }
    }

    public void close() {
        if (status != OrderStatus.ReservationSuccess && status != OrderStatus.PaymentRejected) {
            throw new InvalidOperationException("Invalid order status:" + status);
        }
        applyEvent(new OrderClosed(conferenceId));
    }

    public OrderSeatAssignments createSeatAssignments() {
        if (status != OrderStatus.Success) {
            throw new InvalidOperationException("Cannot create seat assignments for an order that isn't success yet.");
        }
        return new OrderSeatAssignments(id, total.getOrderLines());
    }

    private void handle(OrderPlaced evnt) {
        id = evnt.getAggregateRootId();
        conferenceId = evnt.getConferenceId();
        total = evnt.getOrderTotal();
        accessCode = evnt.getAccessCode();
        status = OrderStatus.Placed;
    }

    private void handle(OrderRegistrantAssigned evnt) {
        registrant = evnt.getRegistrant();
    }

    private void handle(OrderReservationConfirmed evnt) {
        status = evnt.getOrderStatus();
    }

    private void handle(OrderPaymentConfirmed evnt) {
        status = evnt.getOrderStatus();
    }

    private void handle(OrderSuccessed evnt) {
        status = OrderStatus.Success;
    }

    private void handle(OrderExpired evnt) {
        status = OrderStatus.Expired;
    }

    private void handle(OrderClosed evnt) {
        status = OrderStatus.Closed;
    }
}
