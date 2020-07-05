package com.microsoft.conference.common.registration.commands.orders;

import org.enodeframework.commanding.Command;
import org.enodeframework.common.utilities.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class PlaceOrder extends Command<String> {
    public String ConferenceId;
    public List<SeatInfo> Seats;

    public PlaceOrder() {
        super(ObjectId.generateNewStringId());
        Seats = new ArrayList<>();
    }
}
