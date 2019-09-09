package com.microsoft.conference.management.domain.Events;

import com.microsoft.conference.management.domain.Models.ConferenceEditableInfo;
import org.enodeframework.eventing.DomainEvent;

public class ConferenceUpdated extends DomainEvent<String> {
    public ConferenceEditableInfo Info;

    public ConferenceUpdated() {
    }

    public ConferenceUpdated(ConferenceEditableInfo info) {
        Info = info;
    }
}
