package com.microsoft.conference.registration.domain.Orders.Events;

import com.microsoft.conference.registration.domain.Orders.Models.Registrant;
import org.enodeframework.eventing.DomainEvent;

public class OrderRegistrantAssigned extends DomainEvent<String> {
    public String ConferenceId;
    public com.microsoft.conference.registration.domain.Orders.Models.Registrant Registrant;

    public OrderRegistrantAssigned() {
    }

    public OrderRegistrantAssigned(String conferenceId, Registrant registrant) {
        ConferenceId = conferenceId;
        Registrant = registrant;
    }
}
