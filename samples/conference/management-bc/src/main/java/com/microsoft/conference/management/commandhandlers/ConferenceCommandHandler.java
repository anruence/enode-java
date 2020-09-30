package com.microsoft.conference.management.commandhandlers;

import com.microsoft.conference.common.management.commands.AddSeatType;
import com.microsoft.conference.common.management.commands.CancelSeatReservation;
import com.microsoft.conference.common.management.commands.CommitSeatReservation;
import com.microsoft.conference.common.management.commands.CreateConference;
import com.microsoft.conference.common.management.commands.MakeSeatReservation;
import com.microsoft.conference.common.management.commands.PublishConference;
import com.microsoft.conference.common.management.commands.RemoveSeatType;
import com.microsoft.conference.common.management.commands.UnpublishConference;
import com.microsoft.conference.common.management.commands.UpdateConference;
import com.microsoft.conference.common.management.commands.UpdateSeatType;
import com.microsoft.conference.management.domain.models.Conference;
import com.microsoft.conference.management.domain.models.ConferenceEditableInfo;
import com.microsoft.conference.management.domain.models.ConferenceInfo;
import com.microsoft.conference.management.domain.models.ConferenceOwner;
import com.microsoft.conference.management.domain.models.ConferenceSlugIndex;
import com.microsoft.conference.management.domain.models.ReservationItem;
import com.microsoft.conference.management.domain.models.SeatTypeInfo;
import com.microsoft.conference.management.domain.services.RegisterConferenceSlugService;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.infrastructure.ILockService;

import java.util.stream.Collectors;

import static org.enodeframework.common.io.Task.await;

public class ConferenceCommandHandler {

    private ILockService lockService;

    private RegisterConferenceSlugService registerConferenceSlugService;

    public ConferenceCommandHandler(ILockService lockService, RegisterConferenceSlugService registerConferenceSlugService) {
        this.lockService = lockService;
        this.registerConferenceSlugService = registerConferenceSlugService;
    }

    public void HandleAsync(ICommandContext context, CreateConference command) {
        lockService.executeInLock(ConferenceSlugIndex.class.getName(), () ->
        {
            Conference conference = new Conference(command.getAggregateRootId(), new ConferenceInfo(
                    command.accessCode,
                    new ConferenceOwner(command.ownerName, command.ownerEmail),
                    command.slug,
                    command.name,
                    command.description,
                    command.location,
                    command.tagline,
                    command.twitterSearch,
                    command.startDate,
                    command.endDate));
            registerConferenceSlugService.RegisterSlug(command.getId(), conference.getId(), command.slug);
            context.add(conference);
        });
    }

    public void HandleAsync(ICommandContext context, UpdateConference command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.update(new ConferenceEditableInfo(
                command.name,
                command.description,
                command.location,
                command.tagline,
                command.twitterSearch,
                command.startDate,
                command.endDate));
    }

    public void HandleAsync(ICommandContext context, PublishConference command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.publish();
    }

    public void HandleAsync(ICommandContext context, UnpublishConference command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.unpublish();
    }

    public void HandleAsync(ICommandContext context, AddSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.addSeat(new SeatTypeInfo(
                command.name,
                command.description,
                command.price), command.quantity);
    }

    public void HandleAsync(ICommandContext context, RemoveSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.removeSeat(command.seatTypeId);
    }

    public void HandleAsync(ICommandContext context, UpdateSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.updateSeat(
                command.seatTypeId,
                new SeatTypeInfo(command.name, command.description, command.price),
                command.quantity);
    }

    public void HandleAsync(ICommandContext context, MakeSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.makeReservation(command.reservationId, command.seats.stream().map(x -> new ReservationItem(x.seatType, x.quantity)).collect(Collectors.toList()));
    }

    public void HandleAsync(ICommandContext context, CommitSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.commitReservation(command.reservationId);
    }

    public void HandleAsync(ICommandContext context, CancelSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.cancelReservation(command.reservationId);
    }
}
