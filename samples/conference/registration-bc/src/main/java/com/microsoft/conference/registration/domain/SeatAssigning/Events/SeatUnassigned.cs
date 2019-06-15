using System;
using ENode.Eventing;

namespace Registration.SeatAssigning
{
    public class SeatUnassigned : DomainEvent<Guid>
    {
        public int Position { get; private set; }

        public SeatUnassigned() { }
        public SeatUnassigned(int position)
        {
            Position = position;
        }
    }
}
