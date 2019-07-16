package com.microsoft.conference.management.domain.Events;

import com.enodeframework.eventing.DomainEvent;
import com.microsoft.conference.management.domain.Models.ConferenceEditableInfo;

public class ConferenceUpdated extends DomainEvent<String> {
    public ConferenceEditableInfo Info;

    public ConferenceUpdated() {
    }

    public ConferenceUpdated(ConferenceEditableInfo info) {
        Info = info;
    }
}
