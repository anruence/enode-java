using System;
using ENode.Commanding;

namespace ConferenceManagement.Commands
{
    public class CancelSeatReservation : Command<Guid>
    {
        public Guid ReservationId { get; set; }

        public CancelSeatReservation() { }
        public CancelSeatReservation(Guid conferenceId, Guid reservationId) : base(conferenceId)
        {
            ReservationId = reservationId;
        }
    }
}
