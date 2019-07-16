package com.microsoft.conference.common.registration.commands.orders;

import org.enodeframework.commanding.Command;

public class MarkAsSuccess extends Command<String> {
    public MarkAsSuccess() {
    }

    public MarkAsSuccess(String orderId) {
        super(orderId);
    }
}
