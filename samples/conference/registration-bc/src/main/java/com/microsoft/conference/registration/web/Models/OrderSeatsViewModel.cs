using System;
using System.Collections.Generic;
using Registration.ReadModel;

namespace Registration.Web.Models
{
    public class OrderSeatsViewModel
    {
        public OrderSeatsViewModel()
        {
            this.SeatAssignments = new List<OrderSeatAssignment>();
        }

        public Guid OrderId { get; set; }
        public IList<OrderSeatAssignment> SeatAssignments { get; set; }
    }
}
