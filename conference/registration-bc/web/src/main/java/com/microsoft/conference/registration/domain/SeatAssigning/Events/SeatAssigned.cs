using System;
using ENode.Eventing;

namespace Registration.SeatAssigning
{
    public class SeatAssigned : DomainEvent<Guid>
    {
        public int Position { get; private set; }
        public SeatType Seat { get; private set; }
        public Attendee Attendee { get; private set; }

        public SeatAssigned() { }
        public SeatAssigned(int position, SeatType seat, Attendee attendee)
        {
            Position = position;
            Seat = seat;
            Attendee = attendee;
        }
    }
}
