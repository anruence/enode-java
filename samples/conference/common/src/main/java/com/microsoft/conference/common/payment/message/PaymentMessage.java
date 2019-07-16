package com.microsoft.conference.common.payment.message;

import com.enodeframework.infrastructure.ApplicationMessage;

public abstract class PaymentMessage extends ApplicationMessage {
    public String PaymentId;
    public String OrderId;
    public String ConferenceId;
}

