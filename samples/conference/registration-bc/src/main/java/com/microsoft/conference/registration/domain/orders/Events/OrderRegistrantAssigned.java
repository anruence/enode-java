package com.microsoft.conference.registration.domain.orders.Events;

import com.microsoft.conference.registration.domain.orders.Models.Registrant;
import org.enodeframework.eventing.DomainEvent;

public class OrderRegistrantAssigned extends DomainEvent<String> {
    public String ConferenceId;
    public com.microsoft.conference.registration.domain.orders.Models.Registrant Registrant;

    public OrderRegistrantAssigned() {
    }

    public OrderRegistrantAssigned(String conferenceId, Registrant registrant) {
        ConferenceId = conferenceId;
        Registrant = registrant;
    }
}
