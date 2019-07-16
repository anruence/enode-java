package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.ConferenceInfo;
import org.enodeframework.eventing.DomainEvent;

public abstract class ConferenceEvent extends DomainEvent<String> {
    private ConferenceInfo info;

    public ConferenceEvent() {
    }

    public ConferenceEvent(ConferenceInfo info) {
        this.info = info;
    }

    public ConferenceInfo getInfo() {
        return info;
    }

    public void setInfo(ConferenceInfo info) {
        this.info = info;
    }
}
