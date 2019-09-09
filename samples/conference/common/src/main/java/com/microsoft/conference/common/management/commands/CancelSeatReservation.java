package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

public class CancelSeatReservation extends Command<String> {
    public String ReservationId;

    public CancelSeatReservation() {
    }

    public CancelSeatReservation(String conferenceId, String reservationId) {
        super(conferenceId);
        ReservationId = reservationId;
    }
}
