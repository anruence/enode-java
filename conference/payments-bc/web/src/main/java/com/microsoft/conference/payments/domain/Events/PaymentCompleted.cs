using System;
using ENode.Eventing;

namespace Payments
{
    public class PaymentCompleted : DomainEvent<Guid>
    {
        public Guid OrderId { get; private set; }
        public Guid ConferenceId { get; private set; }

        public PaymentCompleted() { }
        public PaymentCompleted(Payment payment, Guid orderId, Guid conferenceId)
        {
            OrderId = orderId;
            ConferenceId = conferenceId;
        }
    }
}
