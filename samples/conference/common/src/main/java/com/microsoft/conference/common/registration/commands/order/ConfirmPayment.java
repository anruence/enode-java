package com.microsoft.conference.common.registration.commands.order;

import org.enodeframework.commanding.Command;

public class ConfirmPayment extends Command<String> {
    public boolean isPaymentSuccess;

    public ConfirmPayment() {
    }

    public ConfirmPayment(String orderId, boolean isPaymentSuccess) {
        super(orderId);
        this.isPaymentSuccess = isPaymentSuccess;
    }
}
