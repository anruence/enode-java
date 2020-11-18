package com.microsoft.conference.common.registration.commands.order;

import lombok.Getter;
import lombok.Setter;
import org.enodeframework.commanding.Command;
import org.enodeframework.common.utilities.IdGenerator;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlaceOrder extends Command<String> {
    private String conferenceId;
    private List<SeatInfo> seatInfos;

    public PlaceOrder() {
        super(IdGenerator.nextId());
        seatInfos = new ArrayList<>();
    }
}
