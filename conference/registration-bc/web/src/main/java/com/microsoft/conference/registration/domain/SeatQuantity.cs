using System;

namespace Registration
{
    [Serializable]
    public class SeatQuantity
    {
        public SeatType Seat { get; private set; }
        public int Quantity { get; private set; }

        public SeatQuantity() { }
        public SeatQuantity(SeatType seat, int quantity)
        {
            Seat = seat;
            Quantity = quantity;
        }
    }
}
