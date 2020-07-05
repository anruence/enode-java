package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.ConferenceInfo;
import org.enodeframework.eventing.DomainEvent;

public abstract class ConferenceEvent extends DomainEvent<String> {
    public ConferenceInfo Info;

    public ConferenceEvent() {
    }

    public ConferenceEvent(ConferenceInfo info) {
        Info = info;
    }
}
