using System;
using System.Collections.Generic;
using ENode.Commanding;

namespace Payments.Commands
{
    public class CreatePayment : Command<Guid>
    {
        public Guid OrderId { get; set; }
        public Guid ConferenceId { get; set; }
        public string Description { get; set; }
        public decimal TotalAmount { get; set; }
        public IEnumerable<PaymentLine> Lines { get; set; }
    }
}
