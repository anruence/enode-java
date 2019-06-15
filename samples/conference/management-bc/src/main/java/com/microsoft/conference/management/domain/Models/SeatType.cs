using System;

namespace ConferenceManagement
{
    public class SeatType
    {
        public Guid Id { get; private set; }
        public SeatTypeInfo Info { get; set; }
        public int Quantity { get; set; }

        public SeatType(Guid id, SeatTypeInfo info)
        {
            Id = id;
            Info = info;
        }
    }
}
