package com.microsoft.conference.common.registration.commands.seatassignments;

import com.microsoft.conference.common.registration.commands.PersonalInfo;
import org.enodeframework.commanding.Command;

public class AssignSeat extends Command<String> {
    public int position;
    public PersonalInfo personalInfo;

    public AssignSeat() {
    }

    public AssignSeat(String orderId) {
        super(orderId);
    }
}
