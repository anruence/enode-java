using System;

namespace Registration.Orders
{
    public class OrderExpired : OrderEvent
    {
        public OrderExpired() { }
        public OrderExpired(Guid conferenceId) : base(conferenceId) { }
    }
}
