using System;
using ENode.Commanding;

namespace ConferenceManagement.Commands
{
    public class RemoveSeatType : Command<Guid>
    {
        public Guid SeatTypeId { get; set; }

        public RemoveSeatType() { }
        public RemoveSeatType(Guid conferenceId) : base(conferenceId) { }
    }
}
