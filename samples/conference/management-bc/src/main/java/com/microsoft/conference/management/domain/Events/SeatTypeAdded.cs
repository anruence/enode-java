using System;

namespace ConferenceManagement
{
    public class SeatTypeAdded : SeatTypeEvent
    {
        public int Quantity { get; private set; }

        public SeatTypeAdded() { }
        public SeatTypeAdded(Guid seatTypeId, SeatTypeInfo seatTypeInfo, int quantity)
            : base(seatTypeId, seatTypeInfo)
        {
            Quantity = quantity;
        }
    }
}
