package com.microsoft.conference.management.domain.events;

import org.enodeframework.eventing.DomainEvent;

public class ConferenceUnpublished extends DomainEvent<String> {
    public ConferenceUnpublished() {
    }
}
