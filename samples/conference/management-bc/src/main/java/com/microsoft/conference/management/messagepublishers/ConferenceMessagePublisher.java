package com.microsoft.conference.management.messagepublishers;

import com.microsoft.conference.common.management.message.SeatInsufficientMessage;
import com.microsoft.conference.common.management.message.SeatReservationItem;
import com.microsoft.conference.common.management.message.SeatsReservationCancelledMessage;
import com.microsoft.conference.common.management.message.SeatsReservationCommittedMessage;
import com.microsoft.conference.common.management.message.SeatsReservedMessage;
import com.microsoft.conference.management.domain.event.SeatsReservationCancelled;
import com.microsoft.conference.management.domain.event.SeatsReservationCommitted;
import com.microsoft.conference.management.domain.event.SeatsReserved;
import com.microsoft.conference.management.domain.publishableexception.SeatInsufficientException;
import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
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

    @Subscribe
    public void handleAsync(SeatsReserved evnt) {
        SeatsReservedMessage message = new SeatsReservedMessage();
        message.conferenceId = evnt.getAggregateRootId();
        message.reservationId = evnt.getReservationId();
        message.reservationItems = evnt.getReservationItems().stream().map(x -> {
            SeatReservationItem item = new SeatReservationItem();
            item.seatTypeId = x.getSeatTypeId();
            item.quantity = x.getQuantity();
            return item;
        }).collect(Collectors.toList());
        await(messagePublisher.publishAsync(message));
    }

    @Subscribe
    public void handleAsync(SeatsReservationCommitted evnt) {
        SeatsReservationCommittedMessage message = new SeatsReservationCommittedMessage();
        message.conferenceId = evnt.getAggregateRootId();
        message.reservationId = evnt.getReservationId();
        await(messagePublisher.publishAsync(message));
    }

    @Subscribe
    public void handleAsync(SeatsReservationCancelled evnt) {
        SeatsReservationCancelledMessage message = new SeatsReservationCancelledMessage();
        message.conferenceId = evnt.getAggregateRootId();
        message.reservationId = evnt.getReservationId();
        await(messagePublisher.publishAsync(message));
    }

    @Subscribe
    public void handleAsync(SeatInsufficientException exception) {
        SeatInsufficientMessage message = new SeatInsufficientMessage();
        message.conferenceId = exception.conferenceId;
        message.reservationId = exception.reservationId;
        await(messagePublisher.publishAsync(message));
    }
}
