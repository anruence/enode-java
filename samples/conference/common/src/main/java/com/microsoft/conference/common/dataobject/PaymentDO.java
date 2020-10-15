package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@TableName(value = "payment")
@Getter
@Setter
public class PaymentDO {
    private Long id;
    private String paymentId;
    private Integer state;
    private String orderId;
    private String description;
    private BigDecimal totalAmount;
    private Integer version;
}
