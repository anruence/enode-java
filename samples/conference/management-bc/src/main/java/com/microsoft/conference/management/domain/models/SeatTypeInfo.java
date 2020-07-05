package com.microsoft.conference.management.domain.models;

import java.math.BigDecimal;

public class SeatTypeInfo {
    public String Name;
    public String Description;
    public BigDecimal Price;

    public SeatTypeInfo(String name, String description, BigDecimal price) {
        Name = name;
        Description = description;
        Price = price;
    }
}
