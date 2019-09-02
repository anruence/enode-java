package com.microsoft.conference.common.management.message;

import com.enodeframework.applicationmessage.ApplicationMessage;

public class SeatsReservationCancelledMessage extends ApplicationMessage {
    public String ConferenceId;
    public String ReservationId;
}
