using System;

namespace ConferenceManagement
{
    public class SeatAvailableQuantity
    {
        public SeatAvailableQuantity(Guid seatTypeId, int availableQuantity)
        {
            SeatTypeId = seatTypeId;
            AvailableQuantity = availableQuantity;
        }

        public Guid SeatTypeId { get; private set; }
        public int AvailableQuantity { get; private set; }
    }
}
