using System;
using System.Collections.Generic;
using ENode.Eventing;

namespace Registration.SeatAssigning
{
    public class OrderSeatAssignmentsCreated : DomainEvent<Guid>
    {
        public Guid OrderId { get; private set; }
        public IEnumerable<SeatAssignment> Assignments { get; private set; }

        public OrderSeatAssignmentsCreated() { }
        public OrderSeatAssignmentsCreated(Guid orderId, IEnumerable<SeatAssignment> assignments)
        {
            OrderId = orderId;
            Assignments = assignments;
        }
    }
}
