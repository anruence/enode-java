using System;
using System.Collections.Generic;
using System.Linq;
using ECommon.Components;

namespace Registration.Orders
{
    [Component]
    public class DefaultPricingService : IPricingService
    {
        public OrderTotal CalculateTotal(Guid conferenceId, IEnumerable<SeatQuantity> seatQuantityList)
        {
            var orderLines = new List<OrderLine>();
            foreach (var seatQuantity in seatQuantityList)
            {
                orderLines.Add(new OrderLine(seatQuantity, Math.Round(seatQuantity.Seat.UnitPrice * seatQuantity.Quantity, 2)));
            }
            return new OrderTotal(orderLines.ToArray(), orderLines.Sum(x => x.LineTotal));
        }
    }
}
