using System;
using ENode.Eventing;

namespace Payments
{
    public class PaymentRejected : DomainEvent<Guid>
    {
        public Guid OrderId { get; private set; }
        public Guid ConferenceId { get; private set; }

        public PaymentRejected() { }
        public PaymentRejected(Guid orderId, Guid conferenceId)
        {
            OrderId = orderId;
            ConferenceId = conferenceId;
        }
    }
}
