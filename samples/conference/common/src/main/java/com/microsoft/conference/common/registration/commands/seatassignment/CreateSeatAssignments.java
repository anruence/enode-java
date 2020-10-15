package com.microsoft.conference.common.registration.commands.seatassignment;

import org.enodeframework.commanding.Command;

public class CreateSeatAssignments extends Command<String> {
    public CreateSeatAssignments() {
    }

    public CreateSeatAssignments(String orderId) {
        super(orderId);
    }
}
