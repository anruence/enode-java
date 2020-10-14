package com.microsoft.conference.registration.domain.orders.events;

import com.microsoft.conference.registration.domain.orders.models.Registrant;
import org.enodeframework.eventing.DomainEvent;

public class OrderRegistrantAssigned extends DomainEvent<String> {
    public String conferenceId;
    public Registrant registrant;

    public OrderRegistrantAssigned() {
    }

    public OrderRegistrantAssigned(String conferenceId, Registrant registrant) {
        this.conferenceId = conferenceId;
        this.registrant = registrant;
    }
}
