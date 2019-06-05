using System;
using ENode.Eventing;

namespace ConferenceManagement
{
    public class ConferencePublished : DomainEvent<Guid>
    {
        public ConferencePublished() { }
    }
}
