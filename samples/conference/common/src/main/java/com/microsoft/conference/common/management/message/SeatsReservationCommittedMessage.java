package com.microsoft.conference.common.management.message;

import org.enodeframework.messaging.ApplicationMessage;

public class SeatsReservationCommittedMessage extends ApplicationMessage {
    public String conferenceId;
    public String reservationId;
}
