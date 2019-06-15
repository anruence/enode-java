using System;

namespace Registration.Orders
{
    public class OrderTotal
    {
        public OrderLine[] Lines { get; private set; }
        public decimal Total { get; private set; }

        public OrderTotal() { }
        public OrderTotal(OrderLine[] lines, decimal total)
        {
            Lines = lines;
            Total = total;
        }
    }
}
