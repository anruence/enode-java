package com.microsoft.conference.management.domain.event;

import org.enodeframework.eventing.DomainEvent;

public class ConferenceUnpublished extends DomainEvent<String> {
    public ConferenceUnpublished() {
    }
}
