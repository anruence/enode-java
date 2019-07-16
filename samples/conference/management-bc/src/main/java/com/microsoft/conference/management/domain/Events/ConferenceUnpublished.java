package com.microsoft.conference.management.domain.Events;

import com.enodeframework.eventing.DomainEvent;

public class ConferenceUnpublished extends DomainEvent<String> {
    public ConferenceUnpublished() {
    }
}
