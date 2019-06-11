using System;

namespace ConferenceManagement
{
    public class SeatQuantity
    {
        public SeatQuantity(Guid seatTypeId, int quantity)
        {
            SeatTypeId = seatTypeId;
            Quantity = quantity;
        }

        public Guid SeatTypeId { get; private set; }
        public int Quantity { get; private set; }
    }
}
