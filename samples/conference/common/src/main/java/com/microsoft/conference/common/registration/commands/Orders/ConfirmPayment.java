package com.microsoft.conference.common.registration.commands.Orders;

import com.enodeframework.commanding.Command;

public class ConfirmPayment extends Command<String> {
    public boolean IsPaymentSuccess;

    public ConfirmPayment() {
    }

    public ConfirmPayment(String orderId, boolean isPaymentSuccess) {
        super(orderId);
        IsPaymentSuccess = isPaymentSuccess;
    }
}
