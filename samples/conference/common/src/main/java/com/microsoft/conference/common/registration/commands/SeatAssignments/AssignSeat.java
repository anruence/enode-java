package com.microsoft.conference.common.registration.commands.SeatAssignments;

import com.enodeframework.commanding.Command;
import com.microsoft.conference.common.registration.commands.PersonalInfo;


public class AssignSeat extends Command<String> {
    public int Position;
    public PersonalInfo PersonalInfo;

    public AssignSeat() {
    }

    public AssignSeat(String orderId) {
        super(orderId);
    }

}
