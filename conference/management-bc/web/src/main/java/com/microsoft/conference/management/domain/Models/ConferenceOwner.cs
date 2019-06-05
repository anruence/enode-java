using System;

namespace ConferenceManagement
{
    public class ConferenceOwner
    {
        public string Name { get; private set; }
        public string Email { get; private set; }

        public ConferenceOwner() { }
        public ConferenceOwner(string name, string email)
        {
            Name = name;
            Email = email;
        }
    }
}
