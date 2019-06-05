using System;

namespace Registration.ReadModel
{
    public class SeatType
    {
        public Guid Id { get; set; }
        public Guid ConferenceId { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        public decimal Price { get; set; }
        public int Quantity { get; set; }
        public int AvailableQuantity { get; set; }
    }
}
