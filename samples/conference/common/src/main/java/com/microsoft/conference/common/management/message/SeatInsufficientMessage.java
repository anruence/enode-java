package com.microsoft.conference.common.management.message;

import org.enodeframework.messaging.ApplicationMessage;

public class SeatInsufficientMessage extends ApplicationMessage {
    public String conferenceId;
    public String reservationId;
}
