package com.microsoft.conference.common.registration.commands.Orders;

import com.enodeframework.commanding.Command;
import com.enodeframework.common.utilities.ObjectId;

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
