using System;
using ENode.Commanding;

namespace Registration.Commands.Orders
{
    public class ConfirmPayment : Command<Guid>
    {
        public bool IsPaymentSuccess { get; set; }

        public ConfirmPayment() { }
        public ConfirmPayment(Guid orderId, bool isPaymentSuccess) : base(orderId)
        {
            IsPaymentSuccess = isPaymentSuccess;
        }
    }
}
