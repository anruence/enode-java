package com.microsoft.conference.registration.domain.order.impl;

import com.microsoft.conference.registration.domain.SeatQuantity;
import com.microsoft.conference.registration.domain.order.PricingService;
import com.microsoft.conference.registration.domain.order.model.OrderLine;
import com.microsoft.conference.registration.domain.order.model.OrderTotal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultPricingServiceImpl implements PricingService {

    @Override
    public OrderTotal calculateTotal(String conferenceId, List<SeatQuantity> seatQuantityList) {
        List<OrderLine> orderLines = new ArrayList<>();
        for (SeatQuantity seatQuantity : seatQuantityList) {
            orderLines.add(new OrderLine(seatQuantity, seatQuantity.getSeatType().getUnitPrice().multiply(BigDecimal.valueOf(seatQuantity.getQuantity()))));
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine x : orderLines) {
            total = total.add(x.getLineTotal());
        }
        return new OrderTotal(orderLines, total);
    }
}
