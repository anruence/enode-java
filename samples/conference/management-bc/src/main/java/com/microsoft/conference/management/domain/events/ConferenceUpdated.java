package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.ConferenceEditableInfo;
import org.enodeframework.eventing.DomainEvent;

public class ConferenceUpdated extends DomainEvent<String> {
    private ConferenceEditableInfo info;

    public ConferenceUpdated() {
    }

    public ConferenceUpdated(ConferenceEditableInfo info) {
        this.info = info;
    }

    public ConferenceEditableInfo getInfo() {
        return info;
    }

    public void setInfo(ConferenceEditableInfo info) {
        this.info = info;
    }
}
