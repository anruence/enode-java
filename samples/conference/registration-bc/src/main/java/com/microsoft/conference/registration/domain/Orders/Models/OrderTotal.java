package com.microsoft.conference.registration.domain.Orders.Models;

import java.math.BigDecimal;
import java.util.List;

public class OrderTotal {
    public List<OrderLine> Lines;
    public BigDecimal Total;

    public OrderTotal() {
    }

    public OrderTotal(List<OrderLine> lines, BigDecimal total) {
        Lines = lines;
        Total = total;
    }
}
