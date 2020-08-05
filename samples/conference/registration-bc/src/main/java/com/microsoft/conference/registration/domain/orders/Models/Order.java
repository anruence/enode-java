package com.microsoft.conference.registration.domain.orders.Models;

import com.microsoft.conference.common.exception.InvalidOperationException;
import com.microsoft.conference.registration.domain.SeatQuantity;
import com.microsoft.conference.registration.domain.orders.Events.OrderClosed;
import com.microsoft.conference.registration.domain.orders.Events.OrderExpired;
import com.microsoft.conference.registration.domain.orders.Events.OrderPaymentConfirmed;
import com.microsoft.conference.registration.domain.orders.Events.OrderPlaced;
import com.microsoft.conference.registration.domain.orders.Events.OrderRegistrantAssigned;
import com.microsoft.conference.registration.domain.orders.Events.OrderReservationConfirmed;
import com.microsoft.conference.registration.domain.orders.Events.OrderSuccessed;
import com.microsoft.conference.registration.domain.orders.IPricingService;
import com.microsoft.conference.registration.domain.seatassigning.Models.OrderSeatAssignments;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.domain.AggregateRoot;

import java.util.Date;
import java.util.List;

public class Order extends AggregateRoot<String> {
    private OrderTotal _total;
    private String _conferenceId;
    private OrderStatus _status;
    private Registrant _registrant;
    private String _accessCode;

    public Order(String id, String conferenceId, List<SeatQuantity> seats, IPricingService pricingService) {
        super(id);
        Ensure.notNullOrEmpty(id, "id");
        Ensure.notNullOrEmpty(conferenceId, "conferenceId");
        Ensure.notNull(seats, "seats");
        Ensure.notNull(pricingService, "pricingService");
        if (seats.isEmpty()) {
            throw new IllegalArgumentException("The seats of order cannot be empty.");
        }
        OrderTotal orderTotal = pricingService.CalculateTotal(conferenceId, seats);
//        Date.UtcNow.add(ConfigSettings.ReservationAutoExpiration)
        applyEvent(new OrderPlaced(conferenceId, orderTotal, new Date(), ObjectId.generateNewStringId()));
    }

    public void AssignRegistrant(String firstName, String lastName, String email) {
        applyEvent(new OrderRegistrantAssigned(_conferenceId, new Registrant(firstName, lastName, email)));
    }

    public void ConfirmReservation(boolean isReservationSuccess) {
        if (_status != OrderStatus.Placed) {
            throw new InvalidOperationException("Invalid order status:" + _status);
        }
        if (isReservationSuccess) {
            applyEvent(new OrderReservationConfirmed(_conferenceId, OrderStatus.ReservationSuccess));
        } else {
            applyEvent(new OrderReservationConfirmed(_conferenceId, OrderStatus.ReservationFailed));
        }
    }

    public void ConfirmPayment(boolean isPaymentSuccess) {
        if (_status != OrderStatus.ReservationSuccess) {
            throw new InvalidOperationException("Invalid order status:" + _status);
        }
        if (isPaymentSuccess) {
            applyEvent(new OrderPaymentConfirmed(_conferenceId, OrderStatus.PaymentSuccess));
        } else {
            applyEvent(new OrderPaymentConfirmed(_conferenceId, OrderStatus.PaymentRejected));
        }
    }

    public void MarkAsSuccess() {
        if (_status != OrderStatus.PaymentSuccess) {
            throw new InvalidOperationException("Invalid order status:" + _status);
        }
        applyEvent(new OrderSuccessed(_conferenceId));
    }

    public void MarkAsExpire() {
        if (_status == OrderStatus.ReservationSuccess) {
            applyEvent(new OrderExpired(_conferenceId));
        }
    }

    public void Close() {
        if (_status != OrderStatus.ReservationSuccess && _status != OrderStatus.PaymentRejected) {
            throw new InvalidOperationException("Invalid order status:" + _status);
        }
        applyEvent(new OrderClosed(_conferenceId));
    }

    public OrderSeatAssignments CreateSeatAssignments() {
        if (_status != OrderStatus.Success) {
            throw new InvalidOperationException("Cannot create seat assignments for an order that isn't success yet.");
        }
        return new OrderSeatAssignments(id, _total.Lines);
    }

    private void Handle(OrderPlaced evnt) {
        id = evnt.getAggregateRootId();
        _conferenceId = evnt.ConferenceId;
        _total = evnt.OrderTotal;
        _accessCode = evnt.AccessCode;
        _status = OrderStatus.Placed;
    }

    private void Handle(OrderRegistrantAssigned evnt) {
        _registrant = evnt.Registrant;
    }

    private void Handle(OrderReservationConfirmed evnt) {
        _status = evnt.orderStatus;
    }

    private void Handle(OrderPaymentConfirmed evnt) {
        _status = evnt.orderStatus;
    }

    private void Handle(OrderSuccessed evnt) {
        _status = OrderStatus.Success;
    }

    private void Handle(OrderExpired evnt) {
        _status = OrderStatus.Expired;
    }

    private void Handle(OrderClosed evnt) {
        _status = OrderStatus.Closed;
    }
}
