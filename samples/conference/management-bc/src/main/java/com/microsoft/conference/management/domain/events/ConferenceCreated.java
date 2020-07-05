package com.microsoft.conference.management.domain.events;

import com.microsoft.conference.management.domain.models.ConferenceInfo;

public class ConferenceCreated extends ConferenceEvent {
    public ConferenceCreated() {
    }

    public ConferenceCreated(ConferenceInfo info) {
        super(info);
    }
}
