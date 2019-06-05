using System;
using ENode.Eventing;

namespace ConferenceManagement
{
    public abstract class SeatTypeEvent : DomainEvent<Guid>
    {
        public Guid SeatTypeId { get; private set; }
        public SeatTypeInfo SeatTypeInfo { get; private set; }

        public SeatTypeEvent() { }
        public SeatTypeEvent(Guid seatTypeId, SeatTypeInfo seatTypeInfo)
        {
            SeatTypeId = seatTypeId;
            SeatTypeInfo = seatTypeInfo;
        }
    }
}
