package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

public class CommitSeatReservation extends Command<String> {
    public String reservationId;

    public CommitSeatReservation() {
    }

    public CommitSeatReservation(String conferenceId, String reservationId) {
        super(conferenceId);
        this.reservationId = reservationId;
    }
}
