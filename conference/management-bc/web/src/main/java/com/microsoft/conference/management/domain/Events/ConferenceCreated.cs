using System;

namespace ConferenceManagement
{
    [Serializable]
    public class ConferenceCreated : ConferenceEvent
    {
        public ConferenceCreated() { }
        public ConferenceCreated(ConferenceInfo info)
            : base(info)
        {
        }
    }
}
