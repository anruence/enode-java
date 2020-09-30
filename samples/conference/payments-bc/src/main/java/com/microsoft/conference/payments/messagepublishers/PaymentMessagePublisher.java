package com.microsoft.conference.payments.messagepublishers;

import com.microsoft.conference.common.payment.message.PaymentCompletedMessage;
import com.microsoft.conference.common.payment.message.PaymentRejectedMessage;
import com.microsoft.conference.payments.domain.events.PaymentCompleted;
import com.microsoft.conference.payments.domain.events.PaymentRejected;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;

import static org.enodeframework.common.io.Task.await;


public class PaymentMessagePublisher {
    private IMessagePublisher<IApplicationMessage> messagePublisher;

    public PaymentMessagePublisher(IMessagePublisher<IApplicationMessage> messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    public void HandleAsync(PaymentCompleted evnt) {
        PaymentCompletedMessage message = new PaymentCompletedMessage();
        message.paymentId = evnt.getAggregateRootId();
        message.conferenceId = evnt.conferenceId;
        message.orderId = evnt.orderId;
        await(messagePublisher.publishAsync(message));
    }

    public void HandleAsync(PaymentRejected evnt) {
        PaymentRejectedMessage message = new PaymentRejectedMessage();
        message.paymentId = evnt.getAggregateRootId();
        message.conferenceId = evnt.conferenceId;
        message.orderId = evnt.orderId;
        await(messagePublisher.publishAsync(message));
    }
}
