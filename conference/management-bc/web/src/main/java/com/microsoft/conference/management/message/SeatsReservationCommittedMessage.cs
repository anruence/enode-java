using System;
using ENode.Infrastructure;

namespace ConferenceManagement.Messages
{
    public class SeatsReservationCommittedMessage : ApplicationMessage
    {
        public Guid ConferenceId { get; set; }
        public Guid ReservationId { get; set; }
    }
}