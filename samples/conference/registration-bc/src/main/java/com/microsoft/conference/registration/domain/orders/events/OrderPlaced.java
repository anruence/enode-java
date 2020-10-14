package com.microsoft.conference.registration.domain.orders.events;

import com.microsoft.conference.registration.domain.orders.models.OrderTotal;
import org.enodeframework.eventing.DomainEvent;

import java.util.Date;

public class OrderPlaced extends DomainEvent<String> {
    public String conferenceId;
    public OrderTotal orderTotal;
    public Date reservationExpirationDate;
    public String accessCode;

    public OrderPlaced() {
    }

    public OrderPlaced(String conferenceId, OrderTotal orderTotal, Date reservationExpirationDate, String accessCode) {
        this.conferenceId = conferenceId;
        this.orderTotal = orderTotal;
        this.reservationExpirationDate = reservationExpirationDate;
        this.accessCode = accessCode;
    }
}
