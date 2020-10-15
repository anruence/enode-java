package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@TableName(value = "payment_item")
@Getter
@Setter
public class PaymentItemDO {
    private Long id;
    private String paymentItemId;
    private String paymentId;
    private String description;
    private BigDecimal amount;
}
