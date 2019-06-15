using System;
using System.Collections.Generic;
using ENode.Eventing;

namespace ConferenceManagement
{
    public class SeatsReservationCommitted : DomainEvent<Guid>
    {
        public Guid ReservationId { get; set; }
        public IEnumerable<SeatQuantity> SeatQuantities { get; set; }

        public SeatsReservationCommitted() { }
        public SeatsReservationCommitted(Guid reservationId, IEnumerable<SeatQuantity> seatQuantities)
        {
            ReservationId = reservationId;
            SeatQuantities = seatQuantities;
        }
    }
}