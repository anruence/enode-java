package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

import java.math.BigDecimal;

public class AddSeatType extends Command<String> {
    public String Name;
    public String Description;
    public BigDecimal Price;
    public int Quantity;

    public AddSeatType() {
    }

    public AddSeatType(String conferenceId) {
        super(conferenceId);
    }
}
