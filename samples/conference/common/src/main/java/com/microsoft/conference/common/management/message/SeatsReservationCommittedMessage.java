package com.microsoft.conference.common.management.message;

import org.enodeframework.applicationmessage.ApplicationMessage;

public class SeatsReservationCommittedMessage extends ApplicationMessage {
    public String ConferenceId;
    public String ReservationId;
}
