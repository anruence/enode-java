using System;
using ENode.Commanding;

namespace Registration.Commands.Orders
{
    public class AssignRegistrantDetails : Command<Guid>
    {
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string Email { get; set; }

        public AssignRegistrantDetails() { }
        public AssignRegistrantDetails(Guid orderId) : base(orderId) { }
    }
}
