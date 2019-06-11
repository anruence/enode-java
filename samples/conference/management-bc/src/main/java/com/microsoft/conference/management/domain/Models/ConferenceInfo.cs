using System;

namespace ConferenceManagement
{
    public class ConferenceInfo
    {
        public string AccessCode { get; private set; }
        public ConferenceOwner Owner { get; private set; }
        public string Slug { get; private set; }
        public string Name { get; private set; }
        public string Description { get; private set; }
        public string Location { get; private set; }
        public string Tagline { get; private set; }
        public string TwitterSearch { get; private set; }
        public DateTime StartDate { get; private set; }
        public DateTime EndDate { get; private set; }

        public ConferenceInfo() { }
        public ConferenceInfo(string accessCode, ConferenceOwner owner, string slug, string name, string description, string location, string tagline, string twitterSearch, DateTime startDate, DateTime endDate)
        {
            AccessCode = accessCode;
            Owner = owner;
            Slug = slug;
            Name = name;
            Description = description;
            Location = location;
            Tagline = tagline;
            TwitterSearch = twitterSearch;
            StartDate = startDate;
            EndDate = endDate;
        }
    }
}
