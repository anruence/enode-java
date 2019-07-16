package com.microsoft.conference.registration.domain.Orders.Events;

import com.enodeframework.eventing.DomainEvent;
import com.microsoft.conference.registration.domain.Orders.Models.Registrant;

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

