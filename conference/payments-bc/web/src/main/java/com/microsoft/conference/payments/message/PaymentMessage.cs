using System;
using ENode.Infrastructure;

namespace Payments.Messages
{
    public abstract class PaymentMessage : ApplicationMessage
    {
        public Guid PaymentId { get; set; }
        public Guid OrderId { get; set; }
        public Guid ConferenceId { get; set; }
    }
}
