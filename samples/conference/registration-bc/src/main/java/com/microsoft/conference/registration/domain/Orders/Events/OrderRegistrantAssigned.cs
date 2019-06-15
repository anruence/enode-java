using System;
using ENode.Eventing;

namespace Registration.Orders
{
    public class OrderRegistrantAssigned : DomainEvent<Guid>
    {
        public Guid ConferenceId { get; private set; }
        public Registrant Registrant { get; private set; }

        public OrderRegistrantAssigned() { }
        public OrderRegistrantAssigned(Guid conferenceId, Registrant registrant)
        {
            ConferenceId = conferenceId;
            Registrant = registrant;
        }
    }
}
