using System;

namespace ConferenceManagement
{
    public class ConferenceEditableInfo
    {
        public string Name { get; private set; }
        public string Description { get; private set; }
        public string Location { get; private set; }
        public string Tagline { get; private set; }
        public string TwitterSearch { get; private set; }
        public DateTime StartDate { get; private set; }
        public DateTime EndDate { get; private set; }

        public ConferenceEditableInfo() { }
        public ConferenceEditableInfo(string name, string description, string location, string tagline, string twitterSearch, DateTime startDate, DateTime endDate)
        {
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
