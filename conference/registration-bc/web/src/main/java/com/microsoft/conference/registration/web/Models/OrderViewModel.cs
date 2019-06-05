using System;
using System.Collections.Generic;

namespace Registration.Web.Models
{
    public class OrderViewModel
    {
        public OrderViewModel()
        {
            this.Items = new List<OrderItemViewModel>();
            this.Seats = new List<SeatQuantity>();
        }

        public Guid OrderId { get; set; }
        public int OrderVersion { get; set; }
        public Guid ConferenceId { get; set; }
        public string ConferenceCode { get; set; }
        public string ConferenceName { get; set; }
        public IList<OrderItemViewModel> Items { get; set; }
        public IList<SeatQuantity> Seats { get; set; }
        public long ReservationExpirationDate { get; set; }
    }
    public class SeatQuantity
    {
        public Guid SeatType { get; set; }
        public int Quantity { get; set; }
    }
}
