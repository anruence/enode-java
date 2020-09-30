package com.microsoft.conference.common.registration.commands.orders;

import org.enodeframework.commanding.Command;
import org.enodeframework.common.utilities.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class PlaceOrder extends Command<String> {
    public String conferenceId;
    public List<SeatInfo> seatInfos;

    public PlaceOrder() {
        super(ObjectId.generateNewStringId());
        seatInfos = new ArrayList<>();
    }
}
