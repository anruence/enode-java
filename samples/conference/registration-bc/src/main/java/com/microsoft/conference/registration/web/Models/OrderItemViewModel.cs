using Registration.ReadModel;

namespace Registration.Web.Models
{
    public class OrderItemViewModel
    {
        public Registration.ReadModel.SeatType SeatType { get; set; }
        public OrderLine OrderLine { get; set; }
        public int Quantity { get; set; }
        public int MaxSelectionQuantity { get; set; }
    }
}
