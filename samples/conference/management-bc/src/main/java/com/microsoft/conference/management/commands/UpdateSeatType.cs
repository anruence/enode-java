using System;
using ENode.Commanding;

namespace ConferenceManagement.Commands
{
    public class UpdateSeatType : Command<Guid>
    {
        public Guid SeatTypeId { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        public decimal Price { get; set; }
        public int Quantity { get; set; }

        public UpdateSeatType() { }
        public UpdateSeatType(Guid conferenceId) : base(conferenceId) { }
    }
}
