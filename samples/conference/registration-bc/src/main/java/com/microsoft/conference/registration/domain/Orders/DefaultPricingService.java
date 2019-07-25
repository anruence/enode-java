package com.microsoft.conference.registration.domain.Orders;

import com.microsoft.conference.registration.domain.Orders.Models.OrderLine;
import com.microsoft.conference.registration.domain.Orders.Models.OrderTotal;
import com.microsoft.conference.registration.domain.SeatQuantity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DefaultPricingService implements IPricingService {
    @Override
    public OrderTotal CalculateTotal(String conferenceId, List<SeatQuantity> seatQuantityList) {
        List<OrderLine> orderLines = new ArrayList<>();
        for (SeatQuantity seatQuantity : seatQuantityList) {
            orderLines.add(new OrderLine(seatQuantity, seatQuantity.Seat.UnitPrice.multiply(BigDecimal.valueOf(seatQuantity.Quantity))));
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine x : orderLines) {
            total = total.add(x.LineTotal);
        }
        return new OrderTotal(orderLines, total);
    }
}
