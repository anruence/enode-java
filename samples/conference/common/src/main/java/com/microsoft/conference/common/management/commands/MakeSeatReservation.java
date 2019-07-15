package com.microsoft.conference.common.management.commands;

import com.enodeframework.commanding.Command;

import java.util.ArrayList;
import java.util.List;

public class MakeSeatReservation extends Command<String> {
    public String ReservationId;
    public List<SeatReservationItemInfo> Seats;

    public MakeSeatReservation() {
    }

    public MakeSeatReservation(String conferenceId) {
        super(conferenceId);
        this.Seats = new ArrayList<>();
    }
}
