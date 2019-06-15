using System.Threading.Tasks;
using ECommon.Components;
using ECommon.IO;
using ENode.Infrastructure;
using Payments.Messages;

namespace Payments.MessagePublishers
{
    [Component]
    public class PaymentMessagePublisher :
        IMessageHandler<PaymentCompleted>,
        IMessageHandler<PaymentRejected>
    {
        private readonly IMessagePublisher<IApplicationMessage> _messagePublisher;

        public PaymentMessagePublisher(IMessagePublisher<IApplicationMessage> messagePublisher)
        {
            _messagePublisher = messagePublisher;
        }

        public Task<AsyncTaskResult> HandleAsync(PaymentCompleted evnt)
        {
            return _messagePublisher.PublishAsync(new PaymentCompletedMessage
            {
                PaymentId = evnt.AggregateRootId,
                ConferenceId = evnt.ConferenceId,
                OrderId = evnt.OrderId
            });
        }
        public Task<AsyncTaskResult> HandleAsync(PaymentRejected evnt)
        {
            return _messagePublisher.PublishAsync(new PaymentRejectedMessage
            {
                PaymentId = evnt.AggregateRootId,
                ConferenceId = evnt.ConferenceId,
                OrderId = evnt.OrderId
            });
        }
    }
}
