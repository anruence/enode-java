using System;
using System.Collections.Generic;

namespace Registration.ReadModel
{
    public class Order
    {
        private IList<OrderLine> _lines = new List<OrderLine>();

        public Guid OrderId { get; set; }
        public Guid ConferenceId { get; set; }
        public int Status { get; set; }
        public string RegistrantEmail { get; set; }
        public string AccessCode { get; set; }
        public decimal TotalAmount { get; set; }
        public DateTime? ReservationExpirationDate { get; set; }
        public bool IsFreeOfCharge()
        {
            return TotalAmount == 0;
        }

        public void SetLines(IList<OrderLine> lines)
        {
            _lines = lines;
        }
        public IList<OrderLine> GetLines()
        {
            return _lines;
        }
    }
    public class OrderLine
    {
        public Guid OrderId { get; set; }
        public Guid SeatTypeId { get; set; }
        public string SeatTypeName { get; set; }
        public int Quantity { get; set; }
        public decimal UnitPrice { get; set; }
        public decimal LineTotal { get; set; }
    }
}
