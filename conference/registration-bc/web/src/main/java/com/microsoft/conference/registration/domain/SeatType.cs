using System;

namespace Registration
{
    [Serializable]
    public class SeatType
    {
        public Guid SeatTypeId { get; private set; }
        public string SeatTypeName { get; private set; }
        public decimal UnitPrice { get; private set; }

        public SeatType() { }
        public SeatType(Guid seatTypeId, string seatTypeName, decimal unitPrice)
        {
            SeatTypeId = seatTypeId;
            SeatTypeName = seatTypeName;
            UnitPrice = unitPrice;
        }
    }
}
