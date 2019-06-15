using System.Linq;
using System.Threading.Tasks;
using ConferenceManagement.Commands;
using ConferenceManagement.Domain.Models;
using ConferenceManagement.Domain.Services;
using ECommon.Components;
using ENode.Commanding;
using ENode.Infrastructure;

namespace ConferenceManagement.CommandHandlers
{
    [Component]
    public class ConferenceCommandHandler :
        ICommandHandler<CreateConference>,
        ICommandHandler<UpdateConference>,
        ICommandHandler<PublishConference>,
        ICommandHandler<UnpublishConference>,
        ICommandHandler<AddSeatType>,
        ICommandHandler<RemoveSeatType>,
        ICommandHandler<UpdateSeatType>,
        ICommandHandler<MakeSeatReservation>,
        ICommandHandler<CommitSeatReservation>,
        ICommandHandler<CancelSeatReservation>
    {
        private readonly ILockService _lockService;
        private readonly RegisterConferenceSlugService _registerConferenceSlugService;

        public ConferenceCommandHandler(ILockService lockService, RegisterConferenceSlugService registerConferenceSlugService)
        {
            _lockService = lockService;
            _registerConferenceSlugService = registerConferenceSlugService;
        }

        public Task HandleAsync(ICommandContext context, CreateConference command)
        {
            return Task.Factory.StartNew(() => _lockService.ExecuteInLock(typeof(ConferenceSlugIndex).Name, () =>
            {
                var conference = new Conference(command.AggregateRootId, new ConferenceInfo(
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
                _registerConferenceSlugService.RegisterSlug(command.Id, conference.Id, command.Slug);
                context.Add(conference);
            }));
        }
        public async Task HandleAsync(ICommandContext context, UpdateConference command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.Update(new ConferenceEditableInfo(
                command.Name,
                command.Description,
                command.Location,
                command.Tagline,
                command.TwitterSearch,
                command.StartDate,
                command.EndDate));
        }
        public async Task HandleAsync(ICommandContext context, PublishConference command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.Publish();
        }
        public async Task HandleAsync(ICommandContext context, UnpublishConference command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.Unpublish();
        }
        public async Task HandleAsync(ICommandContext context, AddSeatType command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.AddSeat(new SeatTypeInfo(
                command.Name,
                command.Description,
                command.Price), command.Quantity);
        }
        public async Task HandleAsync(ICommandContext context, RemoveSeatType command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.RemoveSeat(command.SeatTypeId);
        }
        public async Task HandleAsync(ICommandContext context, UpdateSeatType command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.UpdateSeat(
                command.SeatTypeId,
                new SeatTypeInfo(command.Name, command.Description, command.Price),
                command.Quantity);
        }
        public async Task HandleAsync(ICommandContext context, MakeSeatReservation command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.MakeReservation(command.ReservationId, command.Seats.Select(x => new ReservationItem(x.SeatType, x.Quantity)).ToList());
        }
        public async Task HandleAsync(ICommandContext context, CommitSeatReservation command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.CommitReservation(command.ReservationId);
        }
        public async Task HandleAsync(ICommandContext context, CancelSeatReservation command)
        {
            var conference = await context.GetAsync<Conference>(command.AggregateRootId);
            conference.CancelReservation(command.ReservationId);
        }
    }
}
