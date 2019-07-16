package com.microsoft.conference.registration.domain.Orders.Events;

import com.enodeframework.eventing.DomainEvent;
import com.microsoft.conference.registration.domain.Orders.Models.OrderTotal;

import java.util.Date;

public class OrderPlaced extends DomainEvent<String> {
    public String ConferenceId;
    public com.microsoft.conference.registration.domain.Orders.Models.OrderTotal OrderTotal;
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
