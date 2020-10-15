package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@TableName(value = "order")
@Getter
@Setter
public class OrderDO {
    private Long id;
    private String orderId;
    private String conferenceId;
    private Integer status;
    private String accessCode;
    private String registrantFirstName;
    private String registrantLastName;
    private String registrantEmail;
    private BigDecimal totalAmount;
    private Date reservationExpirationDate;
    private Integer version;
}
