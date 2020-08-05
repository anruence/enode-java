package com.microsoft.conference.registration.domain.orders.Events;

import com.microsoft.conference.registration.domain.orders.Models.OrderTotal;
import org.enodeframework.eventing.DomainEvent;

import java.util.Date;

public class OrderPlaced extends DomainEvent<String> {
    public String ConferenceId;
    public OrderTotal OrderTotal;
    public Date ReservationExpirationDate;
    public String AccessCode;

    public OrderPlaced() {
    }

    public OrderPlaced(String conferenceId, OrderTotal orderTotal, Date reservationExpirationDate, String accessCode) {
        ConferenceId = conferenceId;
        OrderTotal = orderTotal;
        ReservationExpirationDate = reservationExpirationDate;
        AccessCode = accessCode;
    }
}
