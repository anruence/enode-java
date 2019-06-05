using System;
using System.Collections.Generic;
using ECommon.Utilities;
using ENode.Domain;

namespace Registration.SeatAssigning
{
    public class Attendee : ValueObject<Attendee>
    {
        public string FirstName { get; private set; }
        public string LastName { get; private set; }
        public string Email { get; private set; }

        public Attendee() { }
        public Attendee(string firstName, string lastName, string email)
        {
            Ensure.NotNull(firstName, "firstName");
            Ensure.NotNull(lastName, "lastName");
            Ensure.NotNull(email, "email");
            FirstName = firstName;
            LastName = lastName;
            Email = email;
        }

        public override IEnumerable<object> GetAtomicValues()
        {
            yield return FirstName;
            yield return LastName;
            yield return Email;
        }
    }
}
