using System;
using ENode.Commanding;

namespace Registration.Commands.Orders
{
    public class MarkAsSuccess : Command<Guid>
    {
        public MarkAsSuccess() { }
        public MarkAsSuccess(Guid orderId) : base(orderId) { }
    }
}
