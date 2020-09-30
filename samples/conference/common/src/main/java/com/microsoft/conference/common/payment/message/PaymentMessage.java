package com.microsoft.conference.common.payment.message;

import org.enodeframework.messaging.ApplicationMessage;

public abstract class PaymentMessage extends ApplicationMessage {
    public String paymentId;
    public String orderId;
    public String conferenceId;
}
