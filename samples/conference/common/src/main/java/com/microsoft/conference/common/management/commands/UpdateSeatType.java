package com.microsoft.conference.common.management.commands;

import com.enodeframework.commanding.Command;

import java.math.BigDecimal;

public class UpdateSeatType extends Command<String> {
    public String SeatTypeId;
    public String Name;
    public String Description;
    public BigDecimal Price;
    public int Quantity;

    public UpdateSeatType() {
    }

    public UpdateSeatType(String conferenceId) {
        super(conferenceId);
    }
}
