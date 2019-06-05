using System;

namespace Payments.Commands
{
    public class PaymentLine
    {
        public Guid Id { get; set; }
        public string Description { get; set; }
        public decimal Amount { get; set; }
    }
}
