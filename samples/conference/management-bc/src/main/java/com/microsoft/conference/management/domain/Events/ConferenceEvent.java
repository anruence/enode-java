package com.microsoft.conference.management.domain.Events;

import com.enodeframework.eventing.DomainEvent;
import com.microsoft.conference.management.domain.Models.ConferenceInfo;

public abstract class ConferenceEvent extends DomainEvent<String> {
    public ConferenceInfo Info;

    public ConferenceEvent() {
    }

    public ConferenceEvent(ConferenceInfo info) {
        Info = info;
    }
}
