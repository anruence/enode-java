package com.microsoft.conference.management.domain.Events;

import com.microsoft.conference.management.domain.Models.ConferenceInfo;

public class ConferenceCreated extends ConferenceEvent {
    public ConferenceCreated() {
    }

    public ConferenceCreated(ConferenceInfo info) {
        super(info);
    }
}
