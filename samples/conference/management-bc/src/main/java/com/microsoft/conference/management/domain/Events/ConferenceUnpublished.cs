using System;
using ENode.Eventing;

namespace ConferenceManagement
{
    public class ConferenceUnpublished : DomainEvent<Guid>
    {
        public ConferenceUnpublished() { }
    }
}
