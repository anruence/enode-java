package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

import java.math.BigDecimal;

public class UpdateSeatType extends Command<String> {
    public String seatTypeId;
    public String name;
    public String description;
    public BigDecimal price;
    public int quantity;

    public UpdateSeatType() {
    }

    public UpdateSeatType(String conferenceId) {
        super(conferenceId);
    }
}
