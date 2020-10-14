package com.microsoft.conference.payments.messagepublishers;

import com.microsoft.conference.common.payment.message.PaymentCompletedMessage;
import com.microsoft.conference.common.payment.message.PaymentRejectedMessage;
import com.microsoft.conference.payments.domain.events.PaymentCompleted;
import com.microsoft.conference.payments.domain.events.PaymentRejected;
import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;

import static org.enodeframework.common.io.Task.await;

@Event
public class PaymentMessagePublisher {

    @Autowired
    private IMessagePublisher<IApplicationMessage> messagePublisher;

    public PaymentMessagePublisher(IMessagePublisher<IApplicationMessage> messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @Subscribe
    public void handleAsync(PaymentCompleted evnt) {
        PaymentCompletedMessage message = new PaymentCompletedMessage();
        message.paymentId = evnt.getAggregateRootId();
        message.conferenceId = evnt.getConferenceId();
        message.orderId = evnt.getOrderId();
        await(messagePublisher.publishAsync(message));
    }

    @Subscribe
    public void handleAsync(PaymentRejected evnt) {
        PaymentRejectedMessage message = new PaymentRejectedMessage();
        message.paymentId = evnt.getAggregateRootId();
        message.conferenceId = evnt.getConferenceId();
        message.orderId = evnt.getOrderId();
        await(messagePublisher.publishAsync(message));
    }
}
