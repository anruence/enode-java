package com.microsoft.conference.common.registration.commands.orders;

import org.enodeframework.commanding.Command;

public class CloseOrder extends Command<String> {
    public CloseOrder() {
    }

    public CloseOrder(String orderId) {
        super(orderId);
    }
}
