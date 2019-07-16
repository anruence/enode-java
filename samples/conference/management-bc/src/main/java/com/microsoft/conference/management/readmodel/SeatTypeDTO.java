package com.microsoft.conference.management.readmodel;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SeatTypeDTO {
    private String id;
    private String name;
    private String description;
    private int quantity;
    private BigDecimal price;
}