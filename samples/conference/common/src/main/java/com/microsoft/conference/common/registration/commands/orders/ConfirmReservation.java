package com.microsoft.conference.common.registration.commands.orders;

import org.enodeframework.commanding.Command;

public class ConfirmReservation extends Command<String> {
    public boolean IsReservationSuccess;

    public ConfirmReservation() {
    }

    public ConfirmReservation(String orderId, boolean isReservationSuccess) {
        super(orderId);
        IsReservationSuccess = isReservationSuccess;
    }
}
