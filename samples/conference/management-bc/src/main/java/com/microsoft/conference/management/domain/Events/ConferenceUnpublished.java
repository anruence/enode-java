package com.microsoft.conference.management.domain.Events;

import org.enodeframework.eventing.DomainEvent;

public class ConferenceUnpublished extends DomainEvent<String> {
    public ConferenceUnpublished() {
    }
}
