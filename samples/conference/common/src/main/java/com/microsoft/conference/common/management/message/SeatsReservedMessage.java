package com.microsoft.conference.common.management.message;

import org.enodeframework.messaging.ApplicationMessage;

import java.util.List;

public class SeatsReservedMessage extends ApplicationMessage {
    public String conferenceId;
    public String reservationId;
    public List<SeatReservationItem> reservationItems;
}
