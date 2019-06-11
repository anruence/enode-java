using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using ENode.Commanding;

namespace Registration.Commands.Orders
{
    public class PlaceOrder : Command<Guid>
    {
        public PlaceOrder() : base(Guid.NewGuid())
        {
            Seats = new Collection<SeatInfo>();
        }

        public Guid ConferenceId { get; set; }
        public IList<SeatInfo> Seats { get; set; }
    }
}
