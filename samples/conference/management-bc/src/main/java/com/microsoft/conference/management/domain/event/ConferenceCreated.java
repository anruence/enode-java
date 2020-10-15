package com.microsoft.conference.management.domain.event;

import com.microsoft.conference.management.domain.model.ConferenceInfo;

public class ConferenceCreated extends ConferenceEvent {
    public ConferenceCreated() {
    }

    public ConferenceCreated(ConferenceInfo info) {
        super(info);
    }
}
