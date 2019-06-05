using System;
using ENode.Commanding;

namespace Registration.Commands.Orders
{
    public class CloseOrder : Command<Guid>
    {
        public CloseOrder() { }
        public CloseOrder(Guid orderId) : base(orderId) { }
    }
}
