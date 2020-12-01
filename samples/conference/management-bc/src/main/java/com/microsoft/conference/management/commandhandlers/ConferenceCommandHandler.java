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
import com.microsoft.conference.management.domain.model.Conference;
import com.microsoft.conference.management.domain.model.ConferenceEditableInfo;
import com.microsoft.conference.management.domain.model.ConferenceInfo;
import com.microsoft.conference.management.domain.model.ConferenceOwner;
import com.microsoft.conference.management.domain.model.ReservationItem;
import com.microsoft.conference.management.domain.model.SeatTypeInfo;
import com.microsoft.conference.management.domain.service.RegisterConferenceSlugService;
import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.commanding.ICommandContext;
import org.enodeframework.infrastructure.ILockService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

import static org.enodeframework.common.io.Task.await;

@Command
public class ConferenceCommandHandler {

    private ILockService lockService;

    @Autowired
    private RegisterConferenceSlugService registerConferenceSlugService;

    @Subscribe
    public void handleAsync(ICommandContext context, CreateConference command) {
        execInternal(context, command);
    }

    private void execInternal(ICommandContext context, CreateConference command) {
        Conference conference = new Conference(command.getAggregateRootId(), new ConferenceInfo(
                command.getAccessCode(),
                new ConferenceOwner(command.getOwnerName(), command.getOwnerEmail()),
                command.getSlug(),
                command.getName(),
                command.getDescription(),
                command.getLocation(),
                command.getTagline(),
                command.getTwitterSearch(),
                command.getStartDate(),
                command.getEndDate()));
        registerConferenceSlugService.registerSlug(command.getId(), conference.getId(), command.getSlug());
        context.add(conference);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, UpdateConference command) {
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

    @Subscribe
    public void handleAsync(ICommandContext context, PublishConference command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.publish();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, UnpublishConference command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.unpublish();
    }

    @Subscribe
    public void handleAsync(ICommandContext context, AddSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.addSeat(new SeatTypeInfo(
                command.name,
                command.description,
                command.price), command.quantity);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, RemoveSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.removeSeat(command.getSeatTypeId());
    }

    @Subscribe
    public void handleAsync(ICommandContext context, UpdateSeatType command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.updateSeat(
                command.seatTypeId,
                new SeatTypeInfo(command.name, command.description, command.price),
                command.quantity);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, MakeSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.makeReservation(command.reservationId, command.seats.stream().map(x -> new ReservationItem(x.seatType, x.quantity)).collect(Collectors.toList()));
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CommitSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.commitReservation(command.reservationId);
    }

    @Subscribe
    public void handleAsync(ICommandContext context, CancelSeatReservation command) {
        Conference conference = await(context.getAsync(command.getAggregateRootId(), Conference.class));
        conference.cancelReservation(command.reservationId);
    }
}
