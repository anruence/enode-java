using System;
using ENode.Eventing;

namespace ConferenceManagement
{
    public abstract class ConferenceEvent : DomainEvent<Guid>
    {
        public ConferenceInfo Info { get; private set; }

        public ConferenceEvent() { }
        public ConferenceEvent(ConferenceInfo info)
        {
            Info = info;
        }
    }
}
