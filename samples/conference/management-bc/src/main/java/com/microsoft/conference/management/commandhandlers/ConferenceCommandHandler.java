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

    private ILockService _lockService;

    private RegisterConferenceSlugService _registerConferenceSlugService;

    public ConferenceCommandHandler(ILockService lockService, RegisterConferenceSlugService registerConferenceSlugService) {
        _lockService = lockService;
        _registerConferenceSlugService = registerConferenceSlugService;
    }

    public void HandleAsync(ICommandContext context, CreateConference command) {
        _lockService.executeInLock(ConferenceSlugIndex.class.getName(), () ->
        {
            Conference conference = new Conference(command.getAggregateRootId(), new ConferenceInfo(
                    command.AccessCode,
                    new ConferenceOwner(command.OwnerName, command.OwnerEmail),
                    command.Slug,
                    command.Name,
                    command.Description,
                    command.Location,
                    command.Tagline,
                    command.TwitterSearch,
                    command.StartDate,
                    command.EndDate));
            _registerConferenceSlugService.RegisterSlug(command.getId(), conference.getId(), command.Slug);
            context.add(conference);
        });
    }

    public void HandleAsync(ICommandContext context, UpdateConference command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.Update(new ConferenceEditableInfo(
                command.Name,
                command.Description,
                command.Location,
                command.Tagline,
                command.TwitterSearch,
                command.StartDate,
                command.EndDate));
    }

    public void HandleAsync(ICommandContext context, PublishConference command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.Publish();
    }

    public void HandleAsync(ICommandContext context, UnpublishConference command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.Unpublish();
    }

    public void HandleAsync(ICommandContext context, AddSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.addSeat(new SeatTypeInfo(
                command.Name,
                command.Description,
                command.Price), command.Quantity);
    }

    public void HandleAsync(ICommandContext context, RemoveSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.removeSeat(command.SeatTypeId);
    }

    public void HandleAsync(ICommandContext context, UpdateSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.UpdateSeat(
                command.SeatTypeId,
                new SeatTypeInfo(command.Name, command.Description, command.Price),
                command.Quantity);
    }

    public void HandleAsync(ICommandContext context, MakeSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.MakeReservation(command.ReservationId, command.Seats.stream().map(x -> new ReservationItem(x.SeatType, x.Quantity)).collect(Collectors.toList()));
    }

    public void HandleAsync(ICommandContext context, CommitSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.CommitReservation(command.ReservationId);
    }

    public void HandleAsync(ICommandContext context, CancelSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.CancelReservation(command.ReservationId);
    }
}
