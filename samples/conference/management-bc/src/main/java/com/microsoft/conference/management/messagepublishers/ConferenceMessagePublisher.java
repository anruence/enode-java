package com.microsoft.conference.management.messagepublishers;

import com.enodeframework.annotation.Event;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.microsoft.conference.common.management.message.SeatInsufficientMessage;
import com.microsoft.conference.common.management.message.SeatReservationItem;
import com.microsoft.conference.common.management.message.SeatsReservationCancelledMessage;
import com.microsoft.conference.common.management.message.SeatsReservationCommittedMessage;
import com.microsoft.conference.common.management.message.SeatsReservedMessage;
import com.microsoft.conference.management.domain.Events.SeatsReservationCancelled;
import com.microsoft.conference.management.domain.Events.SeatsReservationCommitted;
import com.microsoft.conference.management.domain.Events.SeatsReserved;
import com.microsoft.conference.management.domain.PublishableExceptions.SeatInsufficientException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

import static com.enodeframework.common.io.Task.await;

/**
 * IMessageHandler<SeatsReserved>,
 * IMessageHandler<SeatsReservationCommitted>,
 * IMessageHandler<SeatsReservationCancelled>,
 * IMessageHandler<SeatInsufficientException>
 */
@Event
public class ConferenceMessagePublisher {
    @Autowired
    private IMessagePublisher<IApplicationMessage> _messagePublisher;

    public ConferenceMessagePublisher(IMessagePublisher<IApplicationMessage> messagePublisher) {
        _messagePublisher = messagePublisher;
    }

    public AsyncTaskResult HandleAsync(SeatsReserved evnt) {

        SeatsReservedMessage message = new SeatsReservedMessage();
        message.ConferenceId = evnt.getAggregateRootId();
        message.ReservationId = evnt.ReservationId;
        message.ReservationItems = evnt.ReservationItems.stream().map(x -> {
            SeatReservationItem item = new SeatReservationItem();
            item.SeatTypeId = x.SeatTypeId;
            item.Quantity = x.Quantity;
            return item;
        }).collect(Collectors.toList());
        return await(_messagePublisher.publishAsync(message));

    }

    public AsyncTaskResult HandleAsync(SeatsReservationCommitted evnt) {
        SeatsReservationCommittedMessage message = new SeatsReservationCommittedMessage();
        message.ConferenceId = evnt.getAggregateRootId();
        message.ReservationId = evnt.ReservationId;
        return await(_messagePublisher.publishAsync(message));
    }

    public AsyncTaskResult HandleAsync(SeatsReservationCancelled evnt) {
        SeatsReservationCancelledMessage message = new SeatsReservationCancelledMessage();
        message.ConferenceId = evnt.getAggregateRootId();
        message.ReservationId = evnt.ReservationId;

        return await(_messagePublisher.publishAsync(message));
    }

    public AsyncTaskResult HandleAsync(SeatInsufficientException exception) {
        SeatInsufficientMessage message = new SeatInsufficientMessage();
        message.ConferenceId = exception.ConferenceId;
        message.ReservationId = exception.ReservationId;
        return await(_messagePublisher.publishAsync(message));
    }
}
