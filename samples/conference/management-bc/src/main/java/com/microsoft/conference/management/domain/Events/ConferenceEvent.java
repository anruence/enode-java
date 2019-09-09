package com.microsoft.conference.management.domain.Events;

import com.microsoft.conference.management.domain.Models.ConferenceInfo;
import org.enodeframework.eventing.DomainEvent;

public abstract class ConferenceEvent extends DomainEvent<String> {
    public ConferenceInfo Info;

    public ConferenceEvent() {
    }

    public ConferenceEvent(ConferenceInfo info) {
        Info = info;
    }
}
