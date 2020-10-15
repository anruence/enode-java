package com.microsoft.conference.registration.domain.order.event;

import com.microsoft.conference.registration.domain.order.model.OrderTotal;
import lombok.Getter;
import lombok.Setter;
import org.enodeframework.eventing.DomainEvent;

import java.util.Date;

@Getter
@Setter
public class OrderPlaced extends DomainEvent<String> {
    private String conferenceId;
    private OrderTotal orderTotal;
    private Date reservationExpirationDate;
    private String accessCode;

    public OrderPlaced() {
    }

    public OrderPlaced(String conferenceId, OrderTotal orderTotal, Date reservationExpirationDate, String accessCode) {
        this.conferenceId = conferenceId;
        this.orderTotal = orderTotal;
        this.reservationExpirationDate = reservationExpirationDate;
        this.accessCode = accessCode;
    }
}
