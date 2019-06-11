using System;

namespace Registration.Orders
{
    public class OrderClosed : OrderEvent
    {
        public OrderClosed() { }
        public OrderClosed(Guid conferenceId) : base(conferenceId) { }
    }
}
