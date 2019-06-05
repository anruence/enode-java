using System;
using ENode.Eventing;

namespace Registration.Orders
{
    public class OrderPlaced : DomainEvent<Guid>
    {
        public Guid ConferenceId { get; private set; }
        public OrderTotal OrderTotal { get; private set; }
        public DateTime ReservationExpirationDate { get; private set; }
        public string AccessCode { get; private set; }

        public OrderPlaced() { }
        public OrderPlaced(Guid conferenceId, OrderTotal orderTotal, DateTime reservationExpirationDate, string accessCode)
        {
            ConferenceId = conferenceId;
            OrderTotal = orderTotal;
            ReservationExpirationDate = reservationExpirationDate;
            AccessCode = accessCode;
        }
    }
}
