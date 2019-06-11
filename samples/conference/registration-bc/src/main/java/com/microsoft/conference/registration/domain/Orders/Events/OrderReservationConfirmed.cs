using System;

namespace Registration.Orders
{
    public class OrderReservationConfirmed : OrderEvent
    {
        public OrderStatus OrderStatus { get; private set; }

        public OrderReservationConfirmed() { }
        public OrderReservationConfirmed(Guid conferenceId, OrderStatus orderStatus)
            : base(conferenceId)
        {
            OrderStatus = orderStatus;
        }
    }
}
