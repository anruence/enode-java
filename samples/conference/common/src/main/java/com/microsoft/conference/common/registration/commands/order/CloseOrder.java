package com.microsoft.conference.common.registration.commands.order;

import org.enodeframework.commanding.Command;

public class CloseOrder extends Command<String> {
    public CloseOrder() {
    }

    public CloseOrder(String orderId) {
        super(orderId);
    }
}
