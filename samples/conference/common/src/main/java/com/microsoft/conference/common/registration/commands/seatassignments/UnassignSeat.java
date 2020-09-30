package com.microsoft.conference.common.registration.commands.seatassignments;

import org.enodeframework.commanding.Command;

public class UnassignSeat extends Command<String> {
    public int position;

    public UnassignSeat() {
    }

    public UnassignSeat(String orderId) {
        super(orderId);
    }
}
