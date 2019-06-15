using System;

namespace Registration.Orders
{
    public class OrderPaymentConfirmed : OrderEvent
    {
        public OrderStatus OrderStatus { get; private set; }

        public OrderPaymentConfirmed() { }
        public OrderPaymentConfirmed(Guid conferenceId, OrderStatus orderStatus)
            : base(conferenceId)
        {
            OrderStatus = orderStatus;
        }
    }
}
