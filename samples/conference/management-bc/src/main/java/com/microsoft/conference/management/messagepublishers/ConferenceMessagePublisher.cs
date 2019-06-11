using System.Linq;
using System.Threading.Tasks;
using ConferenceManagement.Messages;
using ECommon.Components;
using ECommon.IO;
using ENode.Infrastructure;

namespace ConferenceManagement.MessagePublishers
{
    [Component]
    public class ConferenceMessagePublisher :
        IMessageHandler<SeatsReserved>,
        IMessageHandler<SeatsReservationCommitted>,
        IMessageHandler<SeatsReservationCancelled>,
        IMessageHandler<SeatInsufficientException>
    {
        private readonly IMessagePublisher<IApplicationMessage> _messagePublisher;

        public ConferenceMessagePublisher(IMessagePublisher<IApplicationMessage> messagePublisher)
        {
            _messagePublisher = messagePublisher;
        }

        public Task<AsyncTaskResult> HandleAsync(SeatsReserved evnt)
        {
            return _messagePublisher.PublishAsync(new SeatsReservedMessage
            {
                ConferenceId = evnt.AggregateRootId,
                ReservationId = evnt.ReservationId,
                ReservationItems = evnt.ReservationItems.Select(x => new SeatReservationItem { SeatTypeId = x.SeatTypeId, Quantity = x.Quantity }).ToList()
            });
        }
        public Task<AsyncTaskResult> HandleAsync(SeatsReservationCommitted evnt)
        {
            return _messagePublisher.PublishAsync(new SeatsReservationCommittedMessage
            {
                ConferenceId = evnt.AggregateRootId,
                ReservationId = evnt.ReservationId
            });
        }
        public Task<AsyncTaskResult> HandleAsync(SeatsReservationCancelled evnt)
        {
            return _messagePublisher.PublishAsync(new SeatsReservationCancelledMessage
            {
                ConferenceId = evnt.AggregateRootId,
                ReservationId = evnt.ReservationId
            });
        }
        public Task<AsyncTaskResult> HandleAsync(SeatInsufficientException exception)
        {
            return _messagePublisher.PublishAsync(new SeatInsufficientMessage
            {
                ConferenceId = exception.ConferenceId,
                ReservationId = exception.ReservationId
            });
        }
    }
}
