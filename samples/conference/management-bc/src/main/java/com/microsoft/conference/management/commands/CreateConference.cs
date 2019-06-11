using System;
using ENode.Commanding;

namespace ConferenceManagement.Commands
{
    public class CreateConference : Command<Guid>
    {
        public string AccessCode { get; set; }
        public string OwnerName { get; set; }
        public string OwnerEmail { get; set; }
        public string Slug { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        public string Location { get; set; }
        public string Tagline { get; set; }
        public string TwitterSearch { get; set; }
        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }
    }
}
