package com.microsoft.conference.management.domain.models;

import java.math.BigDecimal;

public class SeatTypeInfo {
    public String name;
    public String description;
    public BigDecimal price;

    public SeatTypeInfo(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
