using System;

namespace ConferenceManagement
{
    public class SeatTypeUpdated : SeatTypeEvent
    {
        public SeatTypeUpdated() { }
        public SeatTypeUpdated(Guid seatTypeId, SeatTypeInfo seatTypeInfo)
            : base(seatTypeId, seatTypeInfo) { }
    }
}
