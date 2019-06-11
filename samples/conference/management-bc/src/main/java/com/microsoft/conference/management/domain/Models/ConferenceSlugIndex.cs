using System;
namespace ConferenceManagement.Domain.Models
{
    public class ConferenceSlugIndex
    {
        public string IndexId { get; private set; }
        public Guid ConferenceId { get; private set; }
        public string Slug { get; private set; }

        public ConferenceSlugIndex(string indexId, Guid conferenceId, string slug)
        {
            IndexId = indexId;
            ConferenceId = conferenceId;
            Slug = slug;
        }
    }
}
