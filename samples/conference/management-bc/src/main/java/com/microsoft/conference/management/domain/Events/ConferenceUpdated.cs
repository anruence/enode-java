using System;
using ENode.Eventing;

namespace ConferenceManagement
{
    public class ConferenceUpdated : DomainEvent<Guid>
    {
        public ConferenceEditableInfo Info { get; private set; }

        public ConferenceUpdated() { }
        public ConferenceUpdated(ConferenceEditableInfo info)
        {
            Info = info;
        }
    }
}
