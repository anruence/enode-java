package com.microsoft.conference.common.management.commands;

import lombok.Getter;
import lombok.Setter;
import org.enodeframework.commanding.Command;

@Getter
@Setter
public class RemoveSeatType extends Command<String> {
    private String seatTypeId;

    public RemoveSeatType() {
    }

    public RemoveSeatType(String conferenceId) {
        super(conferenceId);
    }
}
