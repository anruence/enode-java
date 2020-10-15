package com.microsoft.conference.common.registration.commands.order;

import org.enodeframework.commanding.Command;

public class MarkAsSuccess extends Command<String> {
    public MarkAsSuccess() {
    }

    public MarkAsSuccess(String orderId) {
        super(orderId);
    }
}
