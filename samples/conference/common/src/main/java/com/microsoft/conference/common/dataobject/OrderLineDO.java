package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@TableName(value = "order_line")
@Getter
@Setter
public class OrderLineDO {
    private Long id;
    private String orderId;
    private String seatTypeId;
    private String seatTypeName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
