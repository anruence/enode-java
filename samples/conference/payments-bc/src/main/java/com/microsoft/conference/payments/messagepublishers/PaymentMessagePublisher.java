package com.microsoft.conference.payments.messagepublishers;

import com.microsoft.conference.common.payment.message.PaymentCompletedMessage;
import com.microsoft.conference.common.payment.message.PaymentRejectedMessage;
import com.microsoft.conference.payments.domain.events.PaymentCompleted;
import com.microsoft.conference.payments.domain.events.PaymentRejected;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;

import static org.enodeframework.common.io.Task.await;


public class PaymentMessagePublisher {
    private IMessagePublisher<IApplicationMessage> _messagePublisher;

    public PaymentMessagePublisher(IMessagePublisher<IApplicationMessage> messagePublisher) {
        _messagePublisher = messagePublisher;
    }

    public void HandleAsync(PaymentCompleted evnt) {
        PaymentCompletedMessage message = new PaymentCompletedMessage();
        message.PaymentId = evnt.getAggregateRootId();
        message.ConferenceId = evnt.ConferenceId;
        message.OrderId = evnt.OrderId;
        await(_messagePublisher.publishAsync(message));
    }

    public void HandleAsync(PaymentRejected evnt) {
        PaymentRejectedMessage message = new PaymentRejectedMessage();
        message.PaymentId = evnt.getAggregateRootId();
        message.ConferenceId = evnt.ConferenceId;
        message.OrderId = evnt.OrderId;
        await(_messagePublisher.publishAsync(message));
    }
}
