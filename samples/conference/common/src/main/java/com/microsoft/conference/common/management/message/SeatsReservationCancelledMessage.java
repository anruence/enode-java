package com.microsoft.conference.common.management.message;

import org.enodeframework.messaging.ApplicationMessage;

public class SeatsReservationCancelledMessage extends ApplicationMessage {
    public String conferenceId;
    public String reservationId;
}
