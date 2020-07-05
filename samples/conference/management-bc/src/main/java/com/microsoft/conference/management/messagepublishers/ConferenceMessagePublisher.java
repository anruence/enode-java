package com.microsoft.conference.management.messagepublishers;

import com.microsoft.conference.common.management.message.SeatInsufficientMessage;
import com.microsoft.conference.common.management.message.SeatReservationItem;
import com.microsoft.conference.common.management.message.SeatsReservationCancelledMessage;
import com.microsoft.conference.common.management.message.SeatsReservationCommittedMessage;
import com.microsoft.conference.common.management.message.SeatsReservedMessage;
import com.microsoft.conference.management.domain.events.SeatsReservationCancelled;
import com.microsoft.conference.management.domain.events.SeatsReservationCommitted;
import com.microsoft.conference.management.domain.events.SeatsReserved;
import com.microsoft.conference.management.domain.publishableexceptions.SeatInsufficientException;
import org.enodeframework.annotation.Event;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

import static org.enodeframework.common.io.Task.await;


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

    public void HandleAsync(SeatsReserved evnt) {
        SeatsReservedMessage message = new SeatsReservedMessage();
        message.ConferenceId = evnt.getAggregateRootId();
        message.ReservationId = evnt.ReservationId;
        message.ReservationItems = evnt.ReservationItems.stream().map(x -> {
            SeatReservationItem item = new SeatReservationItem();
            item.SeatTypeId = x.SeatTypeId;
            item.Quantity = x.Quantity;
            return item;
        }).collect(Collectors.toList());
        await(_messagePublisher.publishAsync(message));
    }

    public void HandleAsync(SeatsReservationCommitted evnt) {
        SeatsReservationCommittedMessage message = new SeatsReservationCommittedMessage();
        message.ConferenceId = evnt.getAggregateRootId();
        message.ReservationId = evnt.ReservationId;
        await(_messagePublisher.publishAsync(message));
    }

    public void HandleAsync(SeatsReservationCancelled evnt) {
        SeatsReservationCancelledMessage message = new SeatsReservationCancelledMessage();
        message.ConferenceId = evnt.getAggregateRootId();
        message.ReservationId = evnt.ReservationId;
        await(_messagePublisher.publishAsync(message));
    }

    public void HandleAsync(SeatInsufficientException exception) {
        SeatInsufficientMessage message = new SeatInsufficientMessage();
        message.ConferenceId = exception.ConferenceId;
        message.ReservationId = exception.ReservationId;
        await(_messagePublisher.publishAsync(message));
    }
}
