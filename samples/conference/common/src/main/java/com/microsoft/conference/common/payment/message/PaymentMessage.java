package com.microsoft.conference.common.payment.message;

import org.enodeframework.messaging.ApplicationMessage;

public abstract class PaymentMessage extends ApplicationMessage {
    public String PaymentId;
    public String OrderId;
    public String ConferenceId;
}
