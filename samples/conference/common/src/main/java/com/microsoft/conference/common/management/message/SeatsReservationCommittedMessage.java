package com.microsoft.conference.common.management.message;

import com.enodeframework.infrastructure.ApplicationMessage;

public class SeatsReservationCommittedMessage extends ApplicationMessage {
    public String ConferenceId;
    public String ReservationId;
}
