using System;
using System.Collections.Generic;
using ENode.Eventing;

namespace ConferenceManagement
{
    public class SeatsReservationCancelled : DomainEvent<Guid>
    {
        public Guid ReservationId { get; set; }
        public IEnumerable<SeatAvailableQuantity> SeatAvailableQuantities { get; set; }

        public SeatsReservationCancelled() { }
        public SeatsReservationCancelled(Guid reservationId, IEnumerable<SeatAvailableQuantity> seatAvailableQuantities)
        {
            ReservationId = reservationId;
            SeatAvailableQuantities = seatAvailableQuantities;
        }
    }
}