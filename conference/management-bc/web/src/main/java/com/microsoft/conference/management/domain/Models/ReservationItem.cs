using System;

namespace ConferenceManagement
{
    public class ReservationItem
    {
        public ReservationItem(Guid seatTypeId, int quantity)
        {
            SeatTypeId = seatTypeId;
            Quantity = quantity;
        }

        public Guid SeatTypeId { get; private set; }
        public int Quantity { get; private set; }
    }
}
