using System;
using ENode.Commanding;

namespace Registration.Commands.SeatAssignments
{
    public class UnassignSeat : Command<Guid>
    {
        public int Position { get; set; }

        public UnassignSeat() { }
        public UnassignSeat(Guid orderId) : base(orderId) { }
    }
}
