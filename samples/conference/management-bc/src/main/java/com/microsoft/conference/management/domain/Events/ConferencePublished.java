package com.microsoft.conference.management.domain.Events;

import org.enodeframework.eventing.DomainEvent;

public class ConferencePublished extends DomainEvent<String> {
    public ConferencePublished() {
    }
}
