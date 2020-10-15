package com.microsoft.conference.registration.domain.order.event;

import com.microsoft.conference.registration.domain.order.model.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderReservationConfirmed extends OrderEvent {
    private OrderStatus orderStatus;

    public OrderReservationConfirmed() {
    }

    public OrderReservationConfirmed(String conferenceId, OrderStatus orderStatus) {
        super(conferenceId);
        this.orderStatus = orderStatus;
    }
}
