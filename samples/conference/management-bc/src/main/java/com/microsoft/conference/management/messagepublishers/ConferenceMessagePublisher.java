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
    private IMessagePublisher<IApplicationMessage> messagePublisher;

    public ConferenceMessagePublisher(IMessagePublisher<IApplicationMessage> messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    public void handleAsync(SeatsReserved evnt) {
        SeatsReservedMessage message = new SeatsReservedMessage();
        message.conferenceId = evnt.getAggregateRootId();
        message.reservationId = evnt.reservationId;
        message.reservationItems = evnt.reservationItems.stream().map(x -> {
            SeatReservationItem item = new SeatReservationItem();
            item.seatTypeId = x.seatTypeId;
            item.quantity = x.quantity;
            return item;
        }).collect(Collectors.toList());
        await(messagePublisher.publishAsync(message));
    }

    public void handleAsync(SeatsReservationCommitted evnt) {
        SeatsReservationCommittedMessage message = new SeatsReservationCommittedMessage();
        message.conferenceId = evnt.getAggregateRootId();
        message.reservationId = evnt.reservationId;
        await(messagePublisher.publishAsync(message));
    }

    public void handleAsync(SeatsReservationCancelled evnt) {
        SeatsReservationCancelledMessage message = new SeatsReservationCancelledMessage();
        message.conferenceId = evnt.getAggregateRootId();
        message.reservationId = evnt.ReservationId;
        await(messagePublisher.publishAsync(message));
    }

    public void handleAsync(SeatInsufficientException exception) {
        SeatInsufficientMessage message = new SeatInsufficientMessage();
        message.conferenceId = exception.conferenceId;
        message.reservationId = exception.reservationId;
        await(messagePublisher.publishAsync(message));
    }
}
