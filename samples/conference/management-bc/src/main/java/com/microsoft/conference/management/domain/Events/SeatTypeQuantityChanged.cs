using System;
using ENode.Eventing;

namespace ConferenceManagement
{
    public class SeatTypeQuantityChanged : DomainEvent<Guid>
    {
        public Guid SeatTypeId { get; private set; }
        public int Quantity { get; private set; }
        public int AvailableQuantity { get; private set; }

        public SeatTypeQuantityChanged() { }
        public SeatTypeQuantityChanged(Guid seatTypeId, int quantity, int availableQuantity)
        {
            SeatTypeId = seatTypeId;
            Quantity = quantity;
            AvailableQuantity = availableQuantity;
        }
    }
}
