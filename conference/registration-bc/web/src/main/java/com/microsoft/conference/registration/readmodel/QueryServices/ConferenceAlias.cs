using System;

namespace Registration.ReadModel
{
    public class ConferenceAlias
    {
        public Guid Id { get; set; }
        public string Slug { get; set; }
        public string Name { get; set; }
        public string Tagline { get; set; }
    }
}
