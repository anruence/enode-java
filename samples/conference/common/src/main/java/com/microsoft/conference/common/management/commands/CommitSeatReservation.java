package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

public class CommitSeatReservation extends Command<String> {
    public String ReservationId;

    public CommitSeatReservation() {
    }

    public CommitSeatReservation(String conferenceId, String reservationId) {
        super(conferenceId);
        ReservationId = reservationId;
    }
}
