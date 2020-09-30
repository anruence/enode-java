package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

import java.math.BigDecimal;

public class AddSeatType extends Command<String> {
    public String name;
    public String description;
    public BigDecimal price;
    public int quantity;

    public AddSeatType() {
    }

    public AddSeatType(String conferenceId) {
        super(conferenceId);
    }
}
