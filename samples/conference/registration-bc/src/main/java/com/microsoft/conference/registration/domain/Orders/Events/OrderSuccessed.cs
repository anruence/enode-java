using System;

namespace Registration.Orders
{
    public class OrderSuccessed : OrderEvent
    {
        public OrderSuccessed() { }
        public OrderSuccessed(Guid conferenceId) : base(conferenceId) { }
    }
}
