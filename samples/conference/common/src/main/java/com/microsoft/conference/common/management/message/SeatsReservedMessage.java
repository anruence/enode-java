package com.microsoft.conference.common.management.message;

import org.enodeframework.applicationmessage.ApplicationMessage;

import java.util.List;

public class SeatsReservedMessage extends ApplicationMessage {
    public String ConferenceId;
    public String ReservationId;
    public List<SeatReservationItem> ReservationItems;
}
