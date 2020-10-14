package com.microsoft.conference.registration.domain.orders.models;

import java.math.BigDecimal;
import java.util.List;

public class OrderTotal {
    public List<OrderLine> orderLines;
    public BigDecimal total;

    public OrderTotal() {
    }

    public OrderTotal(List<OrderLine> orderLines, BigDecimal total) {
        this.orderLines = orderLines;
        this.total = total;
    }
}
