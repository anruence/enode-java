package com.microsoft.conference.registration.readmodel.service;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SeatTypeVO {
    private String id;
    private String conferenceId;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private int availableQuantity;
}
