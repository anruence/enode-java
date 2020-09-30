package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

public class CancelSeatReservation extends Command<String> {
    public String reservationId;

    public CancelSeatReservation() {
    }

    public CancelSeatReservation(String conferenceId, String reservationId) {
        super(conferenceId);
        this.reservationId = reservationId;
    }
}
