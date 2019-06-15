using System;
namespace Registration.ReadModel
{
    public class OrderSeatAssignment
    {
        public Guid AssignmentsId { get; set; }
        public int Position { get; set; }
        public string SeatTypeName { get; set; }
        public string AttendeeEmail { get; set; }
        public string AttendeeFirstName { get; set; }
        public string AttendeeLastName { get; set; }
    }
}
