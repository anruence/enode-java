using System;
using ENode.Commanding;

namespace ConferenceManagement.Commands
{
    public class CommitSeatReservation : Command<Guid>
    {
        public Guid ReservationId { get; set; }

        public CommitSeatReservation() { }
        public CommitSeatReservation(Guid conferenceId, Guid reservationId) : base(conferenceId)
        {
            ReservationId = reservationId;
        }
    }
}
