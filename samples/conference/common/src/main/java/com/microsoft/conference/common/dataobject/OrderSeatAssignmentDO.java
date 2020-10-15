package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName(value = "order_seat_assignment")
@Getter
@Setter
public class OrderSeatAssignmentDO {
    private Long id;
    private String assignmentId;
    private String orderId;
    private Integer position;
    private String seatTypeId;
    private String seatTypeName;
    private String attendeeFirstName;
    private String attendeeLastName;
    private String attendeeEmail;
}
