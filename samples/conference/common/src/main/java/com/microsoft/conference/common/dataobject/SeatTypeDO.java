package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@TableName(value = "conference_seat_type")
@Getter
@Setter
public class SeatTypeDO {
    private Long id;
    private String seatTypeId;
    private String conferenceId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Integer availableQuantity;
}
