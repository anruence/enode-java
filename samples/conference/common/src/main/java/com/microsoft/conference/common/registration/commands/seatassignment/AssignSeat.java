package com.microsoft.conference.common.registration.commands.seatassignment;

import com.microsoft.conference.common.registration.commands.PersonalInfo;
import lombok.Getter;
import lombok.Setter;
import org.enodeframework.commanding.Command;

@Getter
@Setter
public class AssignSeat extends Command<String> {
    private int position;
    private PersonalInfo personalInfo;

    public AssignSeat() {
    }

    public AssignSeat(String orderId) {
        super(orderId);
    }
}
