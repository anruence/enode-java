package com.microsoft.conference.common.registration.commands.SeatAssignments;

import com.microsoft.conference.common.registration.commands.PersonalInfo;
import org.enodeframework.commanding.Command;

public class AssignSeat extends Command<String> {
    public int Position;
    public PersonalInfo PersonalInfo;

    public AssignSeat() {
    }

    public AssignSeat(String orderId) {
        super(orderId);
    }
}
