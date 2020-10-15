package com.microsoft.conference.registration.domain.order.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderTotal {
    private List<OrderLine> orderLines;
    private BigDecimal total;

    public OrderTotal() {
    }

    public OrderTotal(List<OrderLine> orderLines, BigDecimal total) {
        this.orderLines = orderLines;
        this.total = total;
    }
}
